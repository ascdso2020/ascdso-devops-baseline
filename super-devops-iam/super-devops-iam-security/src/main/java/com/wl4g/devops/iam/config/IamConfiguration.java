/*
 * Copyright 2017 ~ 2025 the original author or authors.
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
package com.wl4g.devops.iam.config;

import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import static com.wl4g.devops.common.constants.IAMDevOpsConstants.URI_S_BASE;
import com.wl4g.devops.common.kit.access.IPAccessControl;
import com.wl4g.devops.iam.authc.credential.GeneralCredentialsHashedMatcher;
import com.wl4g.devops.iam.authc.credential.Oauth2AuthorizingBoundMatcher;
import com.wl4g.devops.iam.authc.credential.SmsCredentialsHashedMatcher;
import com.wl4g.devops.iam.authc.credential.secure.DefaultCredentialsSecurer;
import com.wl4g.devops.iam.authc.credential.secure.IamCredentialsSecurer;
import com.wl4g.devops.iam.authc.pam.ExceptionModularRealmAuthenticator;
import com.wl4g.devops.iam.common.cache.EnhancedCacheManager;
import com.wl4g.devops.iam.common.cache.JedisCacheManager;
import com.wl4g.devops.iam.common.config.AbstractIamConfiguration;
import com.wl4g.devops.iam.common.config.AbstractIamProperties;
import com.wl4g.devops.iam.common.config.AbstractIamProperties.ParamProperties;
import com.wl4g.devops.iam.common.mgt.IamSubjectFactory;
import com.wl4g.devops.iam.common.session.mgt.IamSessionFactory;
import com.wl4g.devops.iam.common.session.mgt.JedisIamSessionDAO;
import com.wl4g.devops.iam.configure.AnynothingSecurityCoprocessor;
import com.wl4g.devops.iam.configure.DefaultSecureConfigureAdapter;
import com.wl4g.devops.iam.configure.SecureConfigureAdapter;
import com.wl4g.devops.iam.configure.ServerSecurityConfigurer;
import com.wl4g.devops.iam.configure.ServerSecurityCoprocessor;
import com.wl4g.devops.iam.filter.AuthenticatorAuthenticationFilter;
import com.wl4g.devops.iam.filter.DingtalkAuthenticationFilter;
import com.wl4g.devops.iam.filter.FacebookAuthenticationFilter;
import com.wl4g.devops.iam.filter.QrcodeAuthenticationFilter;
import com.wl4g.devops.iam.filter.ROOTAuthenticationFilter;
import com.wl4g.devops.iam.filter.SinaAuthenticationFilter;
import com.wl4g.devops.iam.filter.GeneralAuthenticationFilter;
import com.wl4g.devops.iam.filter.GithubAuthenticationFilter;
import com.wl4g.devops.iam.filter.GoogleAuthenticationFilter;
import com.wl4g.devops.iam.filter.InternalWhiteListServerAuthenticationFilter;
import com.wl4g.devops.iam.filter.LogoutAuthenticationFilter;
import com.wl4g.devops.iam.filter.QQAuthenticationFilter;
import com.wl4g.devops.iam.filter.SmsAuthenticationFilter;
import com.wl4g.devops.iam.filter.TwitterAuthenticationFilter;
import com.wl4g.devops.iam.filter.WechatAuthenticationFilter;
import com.wl4g.devops.iam.filter.WechatMpAuthenticationFilter;
import com.wl4g.devops.iam.handler.CentralAuthenticationHandler;
import com.wl4g.devops.iam.handler.verification.DefaultJdkJPEGSecurityVerifier;
import com.wl4g.devops.iam.handler.verification.SmsSecurityVerifier;
import com.wl4g.devops.iam.handler.verification.SmsSecurityVerifier.SmsHandleSender;
import com.wl4g.devops.iam.handler.verification.SecurityVerifier;
import com.wl4g.devops.iam.realm.AbstractIamAuthorizingRealm;
import com.wl4g.devops.iam.realm.DingtalkAuthorizingRealm;
import com.wl4g.devops.iam.realm.FacebookAuthorizingRealm;
import com.wl4g.devops.iam.realm.QrcodeAuthorizingRealm;
import com.wl4g.devops.iam.realm.SinaAuthorizingRealm;
import com.wl4g.devops.iam.realm.GeneralAuthorizingRealm;
import com.wl4g.devops.iam.realm.GithubAuthorizingRealm;
import com.wl4g.devops.iam.realm.GoogleAuthorizingRealm;
import com.wl4g.devops.iam.realm.QQAuthorizingRealm;
import com.wl4g.devops.iam.realm.SmsAuthorizingRealm;
import com.wl4g.devops.iam.realm.TwitterAuthorizingRealm;
import com.wl4g.devops.iam.realm.WechatAuthorizingRealm;
import com.wl4g.devops.iam.realm.WechatMpAuthorizingRealm;
import com.wl4g.devops.iam.session.mgt.IamServerSessionManager;
import com.wl4g.devops.iam.web.CentralAuthenticatorController;
import com.wl4g.devops.iam.handler.verification.SmsSecurityVerifier.PrintSmsHandleSender;

public class IamConfiguration extends AbstractIamConfiguration {

	final public static String BEAN_ROOT_FILTER = "rootAuthenticationFilter";
	final public static String BEAN_AUTH_FILTER = "authenticatorAuthenticationFilter";
	final public static String BEAN_OAUTH2_MATCHER = "oauth2BoundMatcher";
	final public static String BEAN_GRAPH_VERIFICATION = "graphBasedVerification";
	final public static String BEAN_SMS_VERIFICATION = "smsVerification";

	// ==============================
	// SHIRO manager and filter's
	// ==============================

	@Bean
	public DefaultWebSecurityManager securityManager(IamSubjectFactory subjectFactory, IamServerSessionManager sessionManager,
			ModularRealmAuthenticator authenticator) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setSessionManager(sessionManager);
		// Register define realm.
		List<Realm> realms = actx.getBeansOfType(AbstractIamAuthorizingRealm.class).values().stream()
				.collect(Collectors.toList());
		securityManager.setRealms(realms);
		securityManager.setSubjectFactory(subjectFactory);
		// Multiple realm authenticator controller
		securityManager.setAuthenticator(authenticator);
		return securityManager;
	}

	@Bean
	public ExceptionModularRealmAuthenticator modularRealmAuthenticator(AuthenticationStrategy authenticationStrategy) {
		ExceptionModularRealmAuthenticator authenticator = new ExceptionModularRealmAuthenticator();
		authenticator.setAuthenticationStrategy(authenticationStrategy);
		List<Realm> realms = actx.getBeansOfType(AbstractIamAuthorizingRealm.class).values().stream()
				.collect(Collectors.toList());
		authenticator.setRealms(realms);
		return authenticator;
	}

	@Bean
	public FirstSuccessfulStrategy firstSuccessfulStrategy() {
		return new FirstSuccessfulStrategy();
	}

	@Bean
	public IamServerSessionManager iamServerSessionManager(IamSessionFactory sessionFactory, JedisIamSessionDAO sessionDao,
			EnhancedCacheManager cacheManager, SimpleCookie cookie, IamProperties config) {
		IamServerSessionManager sessionManager = new IamServerSessionManager(config);
		sessionManager.setSessionFactory(sessionFactory);
		sessionManager.setSessionDAO(sessionDao);
		sessionManager.setSessionIdCookie(cookie);
		sessionManager.setCacheManager(cacheManager);
		sessionManager.setSessionIdUrlRewritingEnabled(config.getSession().isUrlRewriting());
		sessionManager.setSessionIdCookieEnabled(true);
		sessionManager.setSessionValidationInterval(config.getSession().getSessionValidationInterval());
		sessionManager.setGlobalSessionTimeout(config.getSession().getGlobalSessionTimeout());
		return sessionManager;
	}

	// ==============================
	// Credentials hashing matcher`s.
	// ==============================

	@Bean
	public GeneralCredentialsHashedMatcher generalCredentialsHashedMatcher(
			@Qualifier(BEAN_GRAPH_VERIFICATION) SecurityVerifier<?> verification) {
		return new GeneralCredentialsHashedMatcher(verification);
	}

	@Bean
	public SmsCredentialsHashedMatcher smsCredentialsHashedMatcher(
			@Qualifier(BEAN_SMS_VERIFICATION) SecurityVerifier<?> verification) {
		return new SmsCredentialsHashedMatcher(verification);
	}

	@Bean(BEAN_OAUTH2_MATCHER)
	public Oauth2AuthorizingBoundMatcher oauth2AuthorizingBoundMatcher(
			@Qualifier(BEAN_GRAPH_VERIFICATION) SecurityVerifier<?> verification) {
		return new Oauth2AuthorizingBoundMatcher(verification);
	}

	// ==============================
	// Credentials securer's.
	// ==============================

	@Bean
	@ConditionalOnMissingBean
	public SecureConfigureAdapter securerConfigureAdapter() {
		return new DefaultSecureConfigureAdapter();
	}

	@Bean
	@ConditionalOnMissingBean
	public IamCredentialsSecurer iamCredentialsSecurer(SecureConfigureAdapter adapter, JedisCacheManager cacheManager) {
		return new DefaultCredentialsSecurer(adapter.configure(), cacheManager);
	}

	// ==============================
	// Authentication filter`s.
	// ==============================

	@Bean(BEAN_AUTH_FILTER)
	public AuthenticatorAuthenticationFilter authenticatorAuthenticationFilter() {
		return new AuthenticatorAuthenticationFilter();
	}

	@Bean(BEAN_ROOT_FILTER)
	public ROOTAuthenticationFilter rootAuthenticationFilter() {
		return new ROOTAuthenticationFilter();
	}

	@Bean
	public InternalWhiteListServerAuthenticationFilter internalWhiteListServerAuthenticationFilter(IPAccessControl control,
			AbstractIamProperties<? extends ParamProperties> config) {
		return new InternalWhiteListServerAuthenticationFilter(control, config);
	}

	@Bean
	public QrcodeAuthenticationFilter qrcodeAuthenticationFilter() {
		return new QrcodeAuthenticationFilter();
	}

	@Bean
	public FacebookAuthenticationFilter facebookAuthenticationFilter() {
		return new FacebookAuthenticationFilter();
	}

	@Bean
	public SmsAuthenticationFilter smsAuthenticationFilter() {
		return new SmsAuthenticationFilter();
	}

	@Bean
	public WechatAuthenticationFilter wechatAuthenticationFilter() {
		return new WechatAuthenticationFilter();
	}

	@Bean
	public WechatMpAuthenticationFilter wechatMpAuthenticationFilter() {
		return new WechatMpAuthenticationFilter();
	}

	@Bean
	public GeneralAuthenticationFilter generalAuthenticationFilter() {
		return new GeneralAuthenticationFilter();
	}

	@Bean
	public LogoutAuthenticationFilter logoutAuthenticationFilter() {
		return new LogoutAuthenticationFilter();
	}

	@Bean
	public DingtalkAuthenticationFilter dingtalkAuthenticationFilter() {
		return new DingtalkAuthenticationFilter();
	}

	@Bean
	public GoogleAuthenticationFilter googleAuthenticationFilter() {
		return new GoogleAuthenticationFilter();
	}

	@Bean
	public TwitterAuthenticationFilter twitterAuthenticationFilter() {
		return new TwitterAuthenticationFilter();
	}

	@Bean
	public QQAuthenticationFilter qqAuthenticationFilter() {
		return new QQAuthenticationFilter();
	}

	@Bean
	public GithubAuthenticationFilter githubAuthenticationFilter() {
		return new GithubAuthenticationFilter();
	}

	@Bean
	public SinaAuthenticationFilter sinaAuthenticationFilter() {
		return new SinaAuthenticationFilter();
	}

	// ==============================
	// Authentication filter`s registration
	// Reference See: http://www.hillfly.com/2017/179.html
	// org.apache.catalina.core.ApplicationFilterChain#internalDoFilter
	// ==============================

	@Bean
	public FilterRegistrationBean authenticateFilterRegistrationBean(
			@Qualifier(BEAN_AUTH_FILTER) AuthenticatorAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean rootFilterRegistrationBean(@Qualifier(BEAN_ROOT_FILTER) ROOTAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean internalServerFilterRegistrationBean(InternalWhiteListServerAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean facebookFilterRegistrationBean(FacebookAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean smsFilterRegistrationBean(SmsAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean qrcodeFilterRegistrationBean(QrcodeAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean wechatFilterRegistrationBean(WechatAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean wechatMpFilterRegistrationBean(WechatMpAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean generalFilterRegistrationBean(GeneralAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean logoutFilterRegistrationBean(LogoutAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean dingtalkFilterRegistrationBean(DingtalkAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean googleFilterRegistrationBean(GoogleAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean qqFilterRegistrationBean(QQAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean twitterFilterRegistrationBean(TwitterAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean githubFilterRegistrationBean(GithubAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean sinaFilterRegistrationBean(SinaAuthenticationFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		registration.setEnabled(false);
		return registration;
	}

	// ==============================
	// Authorizing realm`s
	// ==============================

	@Bean
	@ConditionalOnMissingBean
	public GeneralAuthorizingRealm generalAuthorizingRealm(GeneralCredentialsHashedMatcher matcher) {
		return new GeneralAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public SmsAuthorizingRealm smsAuthorizingRealm(SmsCredentialsHashedMatcher matcher) {
		return new SmsAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public QrcodeAuthorizingRealm qrcodeAuthorizingRealm(GeneralCredentialsHashedMatcher matcher) {
		return new QrcodeAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public FacebookAuthorizingRealm facebookAuthorizingRealm(
			@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new FacebookAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public WechatAuthorizingRealm wechatAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new WechatAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public WechatMpAuthorizingRealm wechatMpAuthorizingRealm(
			@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new WechatMpAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public DingtalkAuthorizingRealm dingtalkAuthorizingRealm(
			@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new DingtalkAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public GoogleAuthorizingRealm googleAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new GoogleAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public QQAuthorizingRealm qqAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new QQAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public TwitterAuthorizingRealm twitterAuthorizingRealm(
			@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new TwitterAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public SinaAuthorizingRealm sinaAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new SinaAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public GithubAuthorizingRealm githubAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new GithubAuthorizingRealm(matcher);
	}

	// ==============================
	// Configuration properties.
	// ==============================

	@Bean
	public IamProperties iamProperties() {
		return new IamProperties();
	}

	// ==============================
	// Authentication handler's
	// ==============================

	@Bean
	public CentralAuthenticationHandler centralAuthenticationHandler(RestTemplate restTemplate,
			ServerSecurityConfigurer configurer) {
		return new CentralAuthenticationHandler(configurer, restTemplate);
	}

	/**
	 * {@link com.wl4g.devops.iam.captcha.config.KaptchaConfiguration#verification}
	 * {@link com.wl4g.devops.iam.captcha.handler.KaptchaCaptchaHandler}. <br/>
	 * Notes for using `@ConditionalOnMissingBean': 1, `@Bean'method return
	 * value type must be the type using `@Autowired' annotation; 2, or use
	 * `Conditional OnMissing Bean'(MyInterface. class) in this way.`
	 * 
	 * @return
	 */
	@Bean(BEAN_GRAPH_VERIFICATION)
	@ConditionalOnMissingBean
	public DefaultJdkJPEGSecurityVerifier defaultJdkJpegSecurityVerifier() {
		return new DefaultJdkJPEGSecurityVerifier();
	}

	@Bean(BEAN_SMS_VERIFICATION)
	@ConditionalOnMissingBean
	public SmsSecurityVerifier smsVerification() {
		return new SmsSecurityVerifier();
	}

	@Bean
	@ConditionalOnMissingBean
	public SmsHandleSender smsHandleSender() {
		return new PrintSmsHandleSender();
	}

	// ==============================
	// IAM controller's
	// ==============================

	@Override
	protected String getMappingPrefix() {
		return URI_S_BASE;
	}

	@Bean
	public CentralAuthenticatorController centralAuthenticatorController() {
		return new CentralAuthenticatorController();
	}

	// ==============================
	// IAM configure's
	// ==============================

	@Bean
	@ConditionalOnMissingBean
	public ServerSecurityCoprocessor serverSecurityCoprocessor() {
		return new AnynothingSecurityCoprocessor();
	}

}