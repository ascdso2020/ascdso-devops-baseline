/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.devops.ci.pipeline;

import com.wl4g.devops.ci.config.CiCdProperties;
import com.wl4g.devops.ci.core.PipelineJobExecutor;
import com.wl4g.devops.ci.pipeline.model.PipelineInfo;
import com.wl4g.devops.ci.service.DependencyService;
import com.wl4g.devops.ci.utils.GitUtils;
import com.wl4g.devops.ci.utils.SSHTool;
import com.wl4g.devops.common.bean.ci.Dependency;
import com.wl4g.devops.common.bean.ci.Project;
import com.wl4g.devops.common.bean.ci.TaskHistory;
import com.wl4g.devops.common.bean.ci.TaskSign;
import com.wl4g.devops.common.bean.ci.dto.TaskResult;
import com.wl4g.devops.common.exception.ci.LockStateException;
import com.wl4g.devops.common.utils.DateUtils;
import com.wl4g.devops.common.utils.codec.AES;
import com.wl4g.devops.dao.ci.ProjectDao;
import com.wl4g.devops.dao.ci.TaskSignDao;
import com.wl4g.devops.shell.utils.ShellContextHolder;
import com.wl4g.devops.support.lock.SimpleRedisLockManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.wl4g.devops.common.constants.CiDevOpsConstants.CI_LOCK;
import static com.wl4g.devops.common.constants.CiDevOpsConstants.LOCK_TIME;

/**
 * Abstract based deploy provider.
 *
 * @author Wangl.sir <983708408@qq.com>
 * @author vjay
 * @date 2019-05-05 17:17:00
 */
public abstract class AbstractPipelineProvider implements PipelineProvider {
	final protected Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected CiCdProperties config;

	@Autowired
	protected DependencyService dependencyService;

	@Autowired
	protected SimpleRedisLockManager lockManager;

	@Autowired
	protected ProjectDao projectDao;

	@Autowired
	protected TaskSignDao taskSignDao;

	@Autowired
	protected PipelineJobExecutor pipelineTaskRunner;

	protected PipelineInfo pipelineInfo;

	/**
	 * sha
	 */
	protected String shaGit;

	/**
	 * md5
	 */
	protected String shaLocal;

	protected TaskResult taskResult = new TaskResult();

	public AbstractPipelineProvider(PipelineInfo info) {
		this.pipelineInfo = info;
		String[] a = info.getProject().getTarPath().split("/");
		this.pipelineInfo.setTarName(a[a.length - 1]);
	}

	public PipelineInfo getPipelineInfo() {
		return pipelineInfo;
	}

	@Override
	public String getShaGit() {
		return shaGit;
	}

	@Override
	public String getShaLocal() {
		return shaLocal;
	}

	public void setShaGit(String shaGit) {
		this.shaGit = shaGit;
	}

	public void setShaLocal(String shaLocal) {
		this.shaLocal = shaLocal;
	}

	@Override
	public TaskResult getTaskResult() {
		return taskResult;
	}

	/**
	 * Execute
	 */
	public abstract void execute() throws Exception;

	/**
	 * Exce command
	 */
	public String exceCommand(String targetHost, String userName, String command, String rsa) throws Exception {
		if (StringUtils.isBlank(command)) {
			return "command is blank";
		}
		String rsaKey = config.getTranform().getCipherKey();
		AES aes = new AES(rsaKey);
		char[] rsaReal = aes.decrypt(rsa).toCharArray();
		String result = command + "\n";
		result += SSHTool.execute(targetHost, userName, command, rsaReal);
		return result;
	}

	/**
	 * Scp + tar + move to basePath
	 */
	public String scpAndTar(String path, String targetHost, String userName, String targetPath, String rsa) throws Exception {
		String result = mkdirs(targetHost, userName, "/home/" + userName + "/tmp", rsa) + "\n";
		// scp
		result += scpToTmp(path, targetHost, userName, rsa) + "\n";
		// tar
		result += tarToTmp(targetHost, userName, path, rsa) + "\n";
		// mkdir--real app path
		// result += mkdirs(targetHost, userName, targetPath, rsa);

		// remove
		result += removeTarPath(targetHost, userName, path, targetPath, rsa);
		// move
		result += moveToTarPath(targetHost, userName, path, targetPath, rsa) + "\n";
		return result;
	}

	/**
	 * Relink
	 */
	public String relink(String targetHost, String targetPath, String userName, String path, String rsa) throws Exception {
		String command = "ln -snf " + targetPath + "/" + subPacknameWithOutPostfix(path) + " "
				+ pipelineInfo.getProject().getLinkAppHome();
		return exceCommand(targetHost, userName, command, rsa);
	}

	/**
	 * Scp To Tmp
	 */
	public String scpToTmp(String path, String targetHost, String userName, String rsa) throws Exception {
		String rsaKey = config.getTranform().getCipherKey();
		AES aes = new AES(rsaKey);
		char[] rsaReal = aes.decrypt(rsa).toCharArray();
		return SSHTool.uploadFile(targetHost, userName, rsaReal, new File(path), "/home/" + userName + "/tmp");
	}

	/**
	 * Unzip in tmp
	 */
	public String tarToTmp(String targetHost, String userName, String path, String rsa) throws Exception {
		String command = "tar -xvf /home/" + userName + "/tmp" + "/" + subPackname(path) + " -C /home/" + userName + "/tmp";
		return exceCommand(targetHost, userName, command, rsa);
	}

	/**
	 * remove tar path
	 */
	public String removeTarPath(String targetHost, String userName, String path, String targetPath, String rsa) throws Exception {
		String s = targetPath + "/" + subPacknameWithOutPostfix(path);
		if (StringUtils.isBlank(s) || s.trim().equals("/")) {
			throw new RuntimeException("bad command");
		}
		String command = "rm -Rf " + targetPath + "/" + subPacknameWithOutPostfix(path);
		return exceCommand(targetHost, userName, command, rsa);
	}

	/**
	 * Move to tar path
	 */
	public String moveToTarPath(String targetHost, String userName, String path, String targetPath, String rsa) throws Exception {
		String command = "mv /home/" + userName + "/tmp" + "/" + subPacknameWithOutPostfix(path) + " " + targetPath + "/"
				+ subPacknameWithOutPostfix(path);
		return exceCommand(targetHost, userName, command, rsa);
	}

	/**
	 * Local back up
	 */
	public String backupLocal(String path, String sign, String alias, String branchName) throws Exception {
		checkPath(config.getBackup().getBaseDir());
		String command = "cp -Rf " + path + " " + config.getBackup().getBaseDir() + "/" + alias + "/" + branchName + "/"
				+ subPackname(path) + "#" + sign;
		return SSHTool.exec(command);
	}

	/**
	 * Get local back up , for rollback
	 */
	public String getBackupLocal(String backFile, String target) throws Exception {
		checkPath(config.getBackup().getBaseDir());
		String command = "cp -Rf " + backFile + " " + target;
		return SSHTool.exec(command);
	}

	/**
	 * Mkdir
	 */
	public String mkdirs(String targetHost, String userName, String path, String rsa) throws Exception {
		String command = "mkdir -p " + path;
		return exceCommand(targetHost, userName, command, rsa);
	}

	/**
	 * Building (maven)
	 */
	public String mvnInstall(String path, TaskResult taskResult) throws Exception {
		// Execution mvn
		String command = "mvn -f " + path + "/pom.xml clean install -Dmaven.test.skip=true";
		return SSHTool.exec(command, inlog -> !ShellContextHolder.isInterruptIfNecessary(), taskResult);
	}

	/**
	 * Building (maven)
	 */
	public String mvnInstall(String path, TaskResult taskResult, String logPath) throws Exception {
		// Execution mvn
		String command = "mvn -f " + path + "/pom.xml clean install -Dmaven.test.skip=true | tee -a " + logPath;
		return SSHTool.exec(command, inlog -> !ShellContextHolder.isInterruptIfNecessary(), taskResult);
	}

	/**
	 * Rollback
	 */
	public void rollback() throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get date to string user for version
	 */
	public String getDateTimeStr() {
		String str = DateUtils.formatDate(new Date(), DateUtils.YMDHM);
		str = str.substring(2);
		str = "-v" + str;
		return str;
	}

	/**
	 * Get Package Name from path
	 */
	public String subPackname(String path) {
		String[] a = path.split("/");
		return a[a.length - 1];
	}

	/**
	 * Get Packname WithOut Postfix from path
	 */
	public String subPacknameWithOutPostfix(String path) {
		String a = subPackname(path);
		return a.substring(0, a.lastIndexOf("."));
	}

	public String replaceMaster(String str) {
		return str.replaceAll("master-", "");
	}

	public void checkPath(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * build
	 *
	 * @param taskHistory
	 * @param taskResult
	 * @throws Exception
	 */
	public void build(TaskHistory taskHistory, TaskResult taskResult, boolean isRollback) throws Exception {
		LinkedHashSet<Dependency> dependencys = dependencyService.getDependencys(taskHistory.getProjectId(), null);
		log.info("Analysis dependencys={}", dependencys);
		for (Dependency dependency : dependencys) {
			checkLock(taskHistory, dependency.getDependentId(), dependency.getDependentId(), dependency.getBranch(), taskResult,
					true, isRollback);
			// Is Continue ? if fail then return
			if (!taskResult.isSuccess()) {
				return;
			}
		}
		checkLock(taskHistory, taskHistory.getProjectId(), null, taskHistory.getBranchName(), taskResult, false, isRollback);
	}

	private void checkLock(TaskHistory taskHistory, Integer projectId, Integer dependencyId, String branch, TaskResult taskResult,
			boolean isDependency, boolean isRollback) throws Exception {
		// ===== redis lock =====
		Lock lock = lockManager.getLock(CI_LOCK + projectId, LOCK_TIME, TimeUnit.MINUTES);
		if (lock.tryLock()) {// needn't wait
			// Do
			try {
				getSourceAndMvnBuild(taskHistory, projectId, dependencyId, branch, taskResult, isDependency, isRollback);
			} finally {
				lock.unlock();
			}
		} else {
			log.info("One Task is running , just waiting and do nothing");
			try {
				if (lock.tryLock(LOCK_TIME, TimeUnit.MINUTES)) {// Wait
					log.info("The task is finish , jemp this project build");
				} else {
					// One Task is running , and Waiting timeout
					throw new LockStateException("One Task is running ,Waiting timeout");
					// TODO
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			} finally {
				lock.unlock();
			}
		}
	}

	private void getSourceAndMvnBuild(TaskHistory taskHistory, Integer projectId, Integer dependencyId, String branch,
			TaskResult taskResult, boolean isDependency, boolean isRollback) throws Exception {
		log.info("build start projectId={}", projectId);
		Project project = projectDao.selectByPrimaryKey(projectId);
		Assert.notNull(project, "project not exist");

		String path = config.getVcs().getGitlab().getWorkspace() + "/" + project.getProjectName();

		if (isRollback) {
			String sha;
			if (isDependency) {
				TaskSign taskSign = taskSignDao.selectByDependencyIdAndTaskId(dependencyId, taskHistory.getRefId());
				Assert.notNull(taskSign, "not found taskSign");
				sha = taskSign.getShaGit();
			} else {
				sha = taskHistory.getShaGit();
			}

			if (GitUtils.checkGitPath(path)) {
				GitUtils.rollback(config.getVcs().getGitlab().getCredentials(), path, sha);
				taskResult.getStringBuffer().append("project rollback success:").append(project.getProjectName()).append("\n");
			} else {
				GitUtils.clone(config.getVcs().getGitlab().getCredentials(), project.getGitUrl(), path, branch);
				taskResult.getStringBuffer().append("project clone success:").append(project.getProjectName()).append("\n");
				GitUtils.rollback(config.getVcs().getGitlab().getCredentials(), path, sha);
				taskResult.getStringBuffer().append("project rollback success:").append(project.getProjectName()).append("\n");
			}
		} else {
			if (GitUtils.checkGitPath(path)) {// 若果目录存在则:chekcout 分支 并 pull
				GitUtils.checkout(config.getVcs().getGitlab().getCredentials(), path, branch);
				taskResult.getStringBuffer().append("project checkout success:").append(project.getProjectName()).append("\n");
			} else { // 若目录不存在: 则clone 项目并 checkout 对应分支
				GitUtils.clone(config.getVcs().getGitlab().getCredentials(), project.getGitUrl(), path, branch);
				taskResult.getStringBuffer().append("project clone success:").append(project.getProjectName()).append("\n");
			}
		}

		// save dependency git sha -- 保存依赖项目的sha，用于回滚时找回对应的 历史依赖项目
		if (isDependency) {
			TaskSign taskSign = new TaskSign();
			taskSign.setTaskId(taskHistory.getId());
			taskSign.setDependenvyId(dependencyId);
			taskSign.setShaGit(GitUtils.getLatestCommitted(path));
			taskSignDao.insertSelective(taskSign);
		}

		String logPath = config.getBuild().getLogBaseDir() + "/" + taskHistory.getId() + ".log";
		// run install command
		String installResult = mvnInstall(path, taskResult, logPath);

		// ===== build end =====
		taskResult.getStringBuffer().append(installResult);

	}

}