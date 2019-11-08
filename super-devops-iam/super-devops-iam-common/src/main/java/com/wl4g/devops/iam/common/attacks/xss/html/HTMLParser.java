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
package com.wl4g.devops.iam.common.attacks.xss.html;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;

/**
 * Parses an HTML fragment or document and calls a breakout instance that is
 * provided in the constructor, which can filter or modify the tags, values and
 * attributes parsed.
 * 
 * This class uses grammar (lexer and parser) generated by ANTLR, which calls
 * public static functions of this class when new parse events occur.
 */
public class HTMLParser {

	// Since the class is static, the threadlocals are used
	// to store process-specific information.
	// It's better to call instance-specific methods, but need to find
	// out how to do that with ANTLR.
	private static ThreadLocal<Node> topNode;
	private static ThreadLocal<Node> currentNode;
	private static ThreadLocal<Node> attrNode;

	/**
	 * The only method that should be called to initiate the process
	 * 
	 * @param is
	 *            The input stream from where to get the data
	 * @param os
	 *            The output stream to write the processed fragment/document to
	 * @param htmlFilter
	 *            An interface called during the processing of the document. Can
	 *            be used to modify elements
	 * @param convertIntoValidXML
	 *            Converts the output into valid XML for XSL processing for
	 *            example
	 */
	public static void process(Reader reader, Writer writer, IHTMLFilter htmlFilter, boolean convertIntoValidXML)
			throws HandlingException {
		try {
			// Open a char stream input for the document
			ANTLRStringStream input = new ANTLRReaderStream(reader);

			// Start lexing the input
			htmlLexerLexer lex = new htmlLexerLexer(input);

			// Tokenstream for the parser.
			CommonTokenStream tokens = new CommonTokenStream(lex);
			htmlParserParser parser = new htmlParserParser(tokens);
			htmlParserParser.document_return root = parser.document();

			// Set up the tree parser
			CommonTreeNodeStream nodes = new CommonTreeNodeStream((Tree) root.getTree());
			htmlTreeParser walker = new htmlTreeParser(nodes);

			// Initialize data structures
			topNode = new ThreadLocal<>();
			currentNode = new ThreadLocal<>();
			attrNode = new ThreadLocal<>();

			// Walk in the entire document using the tree parser.
			walker.document();

			// Get the top node
			TagNode top = (TagNode) topNode.get();

			// Write the clean document out.
			top.writeAll(writer, htmlFilter, convertIntoValidXML, false);
		} catch (IOException ioe) {
			throw new HandlingException("Could not parse document");
		} catch (RecognitionException re) {
			throw new HandlingException("Could not parse document");
		}
	}

	/**
	 * Notifies the opening of a new tag
	 * 
	 * @param tagName
	 *            The name of the tag
	 * @throws IOException
	 */
	static void openTag(String tagName) throws IOException {
		TagNode node = (TagNode) topNode.get();
		if (node == null) {
			node = new TagNode(tagName.toLowerCase());
			topNode.set(node);
			currentNode.set(node);
		} else {
			TagNode curNode = (TagNode) currentNode.get();
			node = new TagNode(tagName.toLowerCase());
			curNode.addNode(node);
			if (node.mayContainOtherTags()) {
				currentNode.set(node);
			}
		}

		attrNode.set(node);
	}

	/**
	 * Adds an attribute
	 * 
	 * @param attributeName
	 *            The name of the attribute added to the tag
	 * @param value
	 *            The value of the attribute
	 * @throws IOException
	 *             Adding this can throw an exception
	 */
	static void addAttribute(String attributeName, String value) throws IOException {
		TagNode curNode = (TagNode) attrNode.get();

		value = value.trim();

		if (value.length() > 1) {
			if ((value.startsWith("=\"")) || (value.startsWith("='")) || (value.startsWith("=`"))) {
				value = value.substring(2);
				value = value.substring(0, value.length() - 1);
			} else {
				value = value.substring(1);
			}
		}

		curNode.addAttribute(attributeName.toLowerCase(), value);
	}

	/**
	 * Finish the addition of attributes
	 * 
	 * @throws IOException
	 *             This can throw an IOException
	 */
	static void finishAttributes() throws IOException {
		Node node = (Node) attrNode.get();
		attrNode.set(node.getPrevNode());
	}

	/**
	 * This method adds a text to the tag.
	 * 
	 * @param text
	 *            The text to add to the tag
	 * @throws IOException
	 *             This method can throw an IOException
	 */
	static void addText(String text) throws IOException {
		if (text == null) {
			return;
		}

		if (text.trim().equals("")) {
			return;
		}

		TagNode curNode = (TagNode) currentNode.get();
		if (curNode.getName().equals("body")) {
			TagNode p = new TagNode("p");
			p.addNode(new TextNode("p", text));
			curNode.addNode(p);
		} else {
			curNode.addNode(new TextNode(curNode.getName(), text));
		}
	}

	/**
	 * Closes the tag
	 * 
	 * @param tagName
	 *            The tag name
	 * @throws IOException
	 *             This method can throw an IOException
	 */
	static void closeTag(String tagName) throws IOException {
		TagNode curNode = (TagNode) currentNode.get();

		TagNode tempNode = new TagNode(tagName);
		if (tempNode.mayContainOtherTags()) {
			while (!curNode.getName().equals(tagName.toLowerCase())) {
				curNode = (TagNode) curNode.getPrevNode();
			}
			currentNode.set(curNode.getPrevNode());
		}
	}

	public static void main(String[] args) throws Exception {
		String value = "<html><head><meta/></head><body>the content ... end <script>alert('fuck')</script></body></html>";
		StringReader reader = new StringReader(value);
		StringWriter writer = new StringWriter();
		HTMLParser.process(reader, writer, new XSSFilter(), true);
		System.out.println(writer.toString());
	}

}