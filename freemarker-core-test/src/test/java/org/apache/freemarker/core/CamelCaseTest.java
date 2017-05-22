/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.freemarker.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.freemarker.core.outputformat.impl.HTMLOutputFormat;
import org.apache.freemarker.core.outputformat.impl.UndefinedOutputFormat;
import org.apache.freemarker.core.util._StringUtil;
import org.apache.freemarker.test.TemplateTest;
import org.apache.freemarker.test.TestConfigurationBuilder;
import org.junit.Test;

public class CamelCaseTest extends TemplateTest {

    @Test
    public void camelCaseSpecialVars() throws IOException, TemplateException {
        setConfiguration(new TestConfigurationBuilder()
                .outputEncoding(StandardCharsets.UTF_8)
                .urlEscapingCharset(StandardCharsets.ISO_8859_1)
                .locale(Locale.GERMANY)
                .build());
        assertOutput("${.dataModel?isHash?c}", "true");
        assertOutput("${.data_model?is_hash?c}", "true");
        assertOutput("${.localeObject.toString()}", "de_DE");
        assertOutput("${.locale_object.toString()}", "de_DE");
        assertOutput("${.templateName!'null'}", "null");
        assertOutput("${.template_name!'null'}", "null");
        assertOutput("${.currentTemplateName!'null'}", "null");
        assertOutput("${.current_template_name!'null'}", "null");
        assertOutput("${.mainTemplateName!'null'}", "null");
        assertOutput("${.main_template_name!'null'}", "null");
        assertOutput("${.outputEncoding}", StandardCharsets.UTF_8.name());
        assertOutput("${.output_encoding}", StandardCharsets.UTF_8.name());
        assertOutput("${.outputFormat}", UndefinedOutputFormat.INSTANCE.getName());
        assertOutput("${.output_format}", UndefinedOutputFormat.INSTANCE.getName());
        assertOutput("${.urlEscapingCharset}", StandardCharsets.ISO_8859_1.name());
        assertOutput("${.url_escaping_charset}", StandardCharsets.ISO_8859_1.name());
        assertOutput("${.currentNode!'-'}", "-");
        assertOutput("${.current_node!'-'}", "-");
    }

    @Test
    public void camelCaseSpecialVarsInErrorMessage() throws IOException, TemplateException {
        assertErrorContains("${.fooBar}", "dataModel", "\\!data_model");
        assertErrorContains("${.foo_bar}", "data_model", "\\!dataModel");
        // [2.4] If camel case will be the recommended style, then this need to be inverted:
        assertErrorContains("${.foo}", "data_model", "\\!dataModel");
        
        assertErrorContains("<#if x><#elseIf y></#if>${.foo}", "dataModel", "\\!data_model");
        assertErrorContains("<#if x><#elseif y></#if>${.foo}", "data_model", "\\!dataModel");

        setConfigurationToCamelCaseNamingConvention();
        assertErrorContains("${.foo}", "dataModel", "\\!data_model");

        setConfigurationToLegacyCaseNamingConvention();
        assertErrorContains("${.foo}", "data_model", "\\!dataModel");
    }
    
    @Test
    public void camelCaseSettingNames() throws IOException, TemplateException {
        assertOutput("<#setting booleanFormat='Y,N'>${true} <#setting booleanFormat='+,-'>${true}", "Y +");
        assertOutput("<#setting boolean_format='Y,N'>${true} <#setting boolean_format='+,-'>${true}", "Y +");
        
        // Still works inside ?interpret
        assertOutput("<@r\"<#setting booleanFormat='Y,N'>${true}\"?interpret />", "Y");
    }
    
    @Test
    public void camelCaseFtlHeaderParameters() throws IOException, TemplateException {
        assertOutput(
                "<#ftl "
                + "stripWhitespace=false "
                + "stripText=true "
                + "outputFormat='" + HTMLOutputFormat.INSTANCE.getName() + "' "
                + "autoEsc=true "
                + "nsPrefixes={} "
                + ">\nx\n<#if true>\n${.outputFormat}\n</#if>\n",
                "\nHTML\n");

        assertOutput(
                "<#ftl "
                + "strip_whitespace=false "
                + "strip_text=true "
                + "output_format='" + HTMLOutputFormat.INSTANCE.getName() + "' "
                + "auto_esc=true "
                + "ns_prefixes={} "
                + ">\nx\n<#if true>\n${.output_format}\n</#if>\n",
                "\nHTML\n");

        assertErrorContains("<#ftl strip_text=true xmlns={}>", "ns_prefixes", "\\!nsPrefixes");
        assertErrorContains("<#ftl stripText=true xmlns={}>", "nsPrefixes");
        
        assertErrorContains("<#ftl stripWhitespace=true strip_text=true>", "naming convention");
        assertErrorContains("<#ftl strip_whitespace=true stripText=true>", "naming convention");
        assertErrorContains("<#ftl stripWhitespace=true>${.foo_bar}", "naming convention");
        assertErrorContains("<#ftl strip_whitespace=true>${.fooBar}", "naming convention");

        setConfiguration(new TestConfigurationBuilder()
                .namingConvention(NamingConvention.CAMEL_CASE)
                .outputEncoding(StandardCharsets.UTF_8)
                .build());
        assertErrorContains("<#ftl strip_whitespace=true>", "naming convention");
        assertOutput("<#ftl stripWhitespace=true>${.outputEncoding}", StandardCharsets.UTF_8.name());
        
        setConfiguration(new TestConfigurationBuilder()
                .namingConvention(NamingConvention.LEGACY)
                .outputEncoding(StandardCharsets.UTF_8)
                .build());
        assertErrorContains("<#ftl stripWhitespace=true>", "naming convention");
        assertOutput("<#ftl strip_whitespace=true>${.output_encoding}", StandardCharsets.UTF_8.name());
        
        setConfiguration(new TestConfigurationBuilder()
                .namingConvention(NamingConvention.AUTO_DETECT)
                .outputEncoding(StandardCharsets.UTF_8)
                .build());
        assertOutput("<#ftl stripWhitespace=true>${.outputEncoding}", StandardCharsets.UTF_8.name());
        assertOutput("<#ftl encoding='iso-8859-1' stripWhitespace=true>${.outputEncoding}", StandardCharsets.UTF_8.name());
        assertOutput("<#ftl stripWhitespace=true encoding='iso-8859-1'>${.outputEncoding}", StandardCharsets.UTF_8.name());
        assertOutput("<#ftl encoding='iso-8859-1' strip_whitespace=true>${.output_encoding}", StandardCharsets.UTF_8.name());
        assertOutput("<#ftl strip_whitespace=true encoding='iso-8859-1'>${.output_encoding}", StandardCharsets.UTF_8.name());
    }
    
    @Test
    public void camelCaseSettingNamesInErrorMessages() throws IOException, TemplateException {
        assertErrorContains("<#setting fooBar=1>", "booleanFormat", "\\!boolean_format");
        assertErrorContains("<#setting foo_bar=1>", "boolean_format", "\\!booleanFormat");
        // [2.4] If camel case will be the recommended style, then this need to be inverted:
        assertErrorContains("<#setting foo=1>", "boolean_format", "\\!booleanFormat");

        assertErrorContains("<#if x><#elseIf y></#if><#setting foo=1>", "booleanFormat", "\\!boolean_format");
        assertErrorContains("<#if x><#elseif y></#if><#setting foo=1>", "boolean_format", "\\!booleanFormat");

        setConfigurationToCamelCaseNamingConvention();
        assertErrorContains("<#setting foo=1>", "booleanFormat", "\\!boolean_format");

        setConfigurationToLegacyCaseNamingConvention();
        assertErrorContains("<#setting foo=1>", "boolean_format", "\\!booleanFormat");
    }
    
    @Test
    public void camelCaseIncludeParameters() throws IOException, TemplateException {
        assertOutput("<#ftl stripWhitespace=true>[<#include 'noSuchTemplate' ignoreMissing=true>]", "[]");
        assertOutput("<#ftl strip_whitespace=true>[<#include 'noSuchTemplate' ignore_missing=true>]", "[]");
        assertErrorContains("<#ftl stripWhitespace=true>[<#include 'noSuchTemplate' ignore_missing=true>]",
                "naming convention", "ignore_missing");
        assertErrorContains("<#ftl strip_whitespace=true>[<#include 'noSuchTemplate' ignoreMissing=true>]",
                "naming convention", "ignoreMissing");
    }
    
    @Test
    public void specialVarsHasBothNamingStyle() throws IOException, TemplateException {
        assertContainsBothNamingStyles(
                new HashSet(Arrays.asList(ASTExpBuiltInVariable.SPEC_VAR_NAMES)),
                new NamePairAssertion() { @Override
                public void assertPair(String name1, String name2) { } });
    }
    
    @Test
    public void camelCaseBuiltIns() throws IOException, TemplateException {
        assertOutput("${'x'?upperCase}", "X");
        assertOutput("${'x'?upper_case}", "X");
    }

    @Test
    public void stringLiteralInterpolation() throws IOException, TemplateException {
        assertEquals(NamingConvention.AUTO_DETECT, getConfiguration().getNamingConvention());
        addToDataModel("x", "x");
        
        assertOutput("${'-${x?upperCase}-'} ${x?upperCase}", "-X- X");
        assertOutput("${x?upperCase} ${'-${x?upperCase}-'}", "X -X-");
        assertOutput("${'-${x?upper_case}-'} ${x?upper_case}", "-X- X");
        assertOutput("${x?upper_case} ${'-${x?upper_case}-'}", "X -X-");

        assertErrorContains("${'-${x?upper_case}-'} ${x?upperCase}",
                "naming convention", "legacy", "upperCase", "detection", "9");
        assertErrorContains("${x?upper_case} ${'-${x?upperCase}-'}",
                "naming convention", "legacy", "upperCase", "detection", "5");
        assertErrorContains("${'-${x?upperCase}-'} ${x?upper_case}",
                "naming convention", "camel", "upper_case");
        assertErrorContains("${x?upperCase} ${'-${x?upper_case}-'}",
                "naming convention", "camel", "upper_case");

        setConfigurationToCamelCaseNamingConvention();
        assertOutput("${'-${x?upperCase}-'} ${x?upperCase}", "-X- X");
        assertErrorContains("${'-${x?upper_case}-'}",
                "naming convention", "camel", "upper_case", "\\!detection");

        setConfigurationToLegacyCaseNamingConvention();
        assertOutput("${'-${x?upper_case}-'} ${x?upper_case}", "-X- X");
        assertErrorContains("${'-${x?upperCase}-'}",
                "naming convention", "legacy", "upperCase", "\\!detection");
    }
    
    @Test
    public void evalAndInterpret() throws IOException, TemplateException {
        assertEquals(NamingConvention.AUTO_DETECT, getConfiguration().getNamingConvention());
        // The naming convention detected doesn't affect the enclosing template's naming convention.
        // - ?eval:
        assertOutput("${\"'x'?upperCase\"?eval}${'x'?upper_case}", "XX");
        assertOutput("${\"'x'?upper_case\"?eval}${'x'?upperCase}", "XX");
        assertOutput("${'x'?upperCase}${\"'x'?upper_case\"?eval}", "XX");
        assertErrorContains("${\"'x'\n?upperCase\n?is_string\"?eval}",
                "naming convention", "camel", "upperCase", "is_string", "line 2", "line 3");
        // - ?interpret:
        assertOutput("<@r\"${'x'?upperCase}\"?interpret />${'x'?upper_case}", "XX");
        assertOutput("<@r\"${'x'?upper_case}\"?interpret />${'x'?upperCase}", "XX");
        assertOutput("${'x'?upper_case}<@r\"${'x'?upperCase}\"?interpret />", "XX");
        assertErrorContains("<@r\"${'x'\n?upperCase\n?is_string}\"?interpret />",
                "naming convention", "camel", "upperCase", "is_string", "line 2", "line 3");
        
        // Will be inherited by ?eval-ed/?interpreted fragments:
        setConfigurationToCamelCaseNamingConvention();
        // - ?eval:
        assertErrorContains("${\"'x'?upper_case\"?eval}", "naming convention", "camel", "upper_case");
        assertOutput("${\"'x'?upperCase\"?eval}", "X");
        // - ?interpret:
        assertErrorContains("<@r\"${'x'?upper_case}\"?interpret />", "naming convention", "camel", "upper_case");
        assertOutput("<@r\"${'x'?upperCase}\"?interpret />", "X");
        
        // Again, will be inherited by ?eval-ed/?interpreted fragments:
        setConfigurationToLegacyCaseNamingConvention();
        // - ?eval:
        assertErrorContains("${\"'x'?upperCase\"?eval}", "naming convention", "legacy", "upperCase");
        assertOutput("${\"'x'?upper_case\"?eval}", "X");
        // - ?interpret:
        assertErrorContains("<@r\"${'x'?upperCase}\"?interpret />", "naming convention", "legacy", "upperCase");
        assertOutput("<@r\"${'x'?upper_case}\"?interpret />", "X");
    }

    private void setConfigurationToLegacyCaseNamingConvention() {
        setConfiguration(new TestConfigurationBuilder()
                .namingConvention(NamingConvention.LEGACY)
                .build());
    }

    @Test
    public void camelCaseBuiltInErrorMessage() throws IOException, TemplateException {
        assertErrorContains("${'x'?upperCasw}", "upperCase", "\\!upper_case");
        assertErrorContains("${'x'?upper_casw}", "upper_case", "\\!upperCase");
        // [2.4] If camel case will be the recommended style, then this need to be inverted:
        assertErrorContains("${'x'?foo}", "upper_case", "\\!upperCase");
        
        assertErrorContains("<#if x><#elseIf y></#if> ${'x'?foo}", "upperCase", "\\!upper_case");
        assertErrorContains("<#if x><#elseif y></#if>${'x'?foo}", "upper_case", "\\!upperCase");

        setConfigurationToCamelCaseNamingConvention();
        assertErrorContains("${'x'?foo}", "upperCase", "\\!upper_case");
        setConfigurationToLegacyCaseNamingConvention();
        assertErrorContains("${'x'?foo}", "upper_case", "\\!upperCase");
    }

    private void setConfigurationToCamelCaseNamingConvention() {
        setConfiguration(new TestConfigurationBuilder()
                .namingConvention(NamingConvention.CAMEL_CASE)
                .build());
    }

    @Test
    public void builtInsHasBothNamingStyle() throws IOException, TemplateException {
        assertContainsBothNamingStyles(getConfiguration().getSupportedBuiltInNames(), new NamePairAssertion() {

            @Override
            public void assertPair(String name1, String name2) {
                ASTExpBuiltIn bi1  = ASTExpBuiltIn.BUILT_INS_BY_NAME.get(name1);
                ASTExpBuiltIn bi2 = ASTExpBuiltIn.BUILT_INS_BY_NAME.get(name2);
                assertTrue("\"" + name1 + "\" and \"" + name2 + "\" doesn't belong to the same BI object.",
                        bi1 == bi2);
            }
            
        });
    }

    private void assertContainsBothNamingStyles(Set<String> names, NamePairAssertion namePairAssertion) {
        Set<String> underscoredNamesWithCamelCasePair = new HashSet<>();
        for (String name : names) {
            if (_StringUtil.getIdentifierNamingConvention(name) == NamingConvention.CAMEL_CASE) {
                String underscoredName = correctIsoBIExceptions(_StringUtil.camelCaseToUnderscored(name)); 
                assertTrue(
                        "Missing underscored variation \"" + underscoredName + "\" for \"" + name + "\".",
                        names.contains(underscoredName));
                assertTrue(underscoredNamesWithCamelCasePair.add(underscoredName));
                
                namePairAssertion.assertPair(name, underscoredName);
            }
        }
        for (String name : names) {
            if (_StringUtil.getIdentifierNamingConvention(name) == NamingConvention.LEGACY) {
                assertTrue("Missing camel case variation for \"" + name + "\".",
                        underscoredNamesWithCamelCasePair.contains(name));
            }
        }
    }
    
    private String correctIsoBIExceptions(String underscoredName) {
        return underscoredName.replace("_n_z", "_nz").replace("_f_z", "_fz");
    }
    
    @Test
    public void camelCaseDirectives() throws IOException, TemplateException {
        camelCaseDirectives(false);
        setConfiguration(new TestConfigurationBuilder()
                .tagSyntax(TagSyntax.AUTO_DETECT)
                .build());
        camelCaseDirectives(true);
    }

    private void camelCaseDirectives(boolean squared) throws IOException, TemplateException {
        assertOutput(
                squared("<#list 1..4 as x><#if x == 1>one <#elseIf x == 2>two <#elseIf x == 3>three "
                        + "<#else>more</#if></#list>", squared),
                "one two three more");
        assertOutput(
                squared("<#list 1..4 as x><#if x == 1>one <#elseif x == 2>two <#elseif x == 3>three "
                        + "<#else>more</#if></#list>", squared),
                "one two three more");
        
        assertOutput(
                squared("<#escape x as x?upperCase>${'a'}<#noEscape>${'b'}</#noEscape></#escape>", squared),
                "Ab");
        assertOutput(
                squared("<#escape x as x?upper_case>${'a'}<#noescape>${'b'}</#noescape></#escape>", squared),
                "Ab");
        
        assertOutput(
                squared("<#noParse></#noparse></#noParse>", squared),
                squared("</#noparse>", squared));
        assertOutput(
                squared("<#noparse></#noParse></#noparse>", squared),
                squared("</#noParse>", squared));
    }
    
    private String squared(String ftl, boolean squared) {
        return squared ? ftl.replace('<', '[').replace('>', ']') : ftl;
    }

    @Test
    public void explicitNamingConvention() throws IOException, TemplateException {
        explicitNamingConvention(false);
        explicitNamingConvention(true);
    }
    
    private void explicitNamingConvention(boolean squared) throws IOException, TemplateException {
        TagSyntax tagSyntax = squared ? TagSyntax.AUTO_DETECT
                : TagSyntax.ANGLE_BRACKET;
        setConfiguration(new TestConfigurationBuilder()
                .tagSyntax(tagSyntax)
                .namingConvention(NamingConvention.CAMEL_CASE)
                .build());

        assertErrorContains(
                squared("<#if true>t<#elseif false>f</#if>", squared),
                "naming convention", "camel", "#elseif");
        assertOutput(
                squared("<#if true>t<#elseIf false>f</#if>", squared),
                "t");
        
        assertErrorContains(
                squared("<#noparse>${x}</#noparse>", squared),
                "naming convention", "camel", "#noparse");
        assertOutput(
                squared("<#noParse>${x}</#noParse>", squared),
                "${x}");
        
        assertErrorContains(
                squared("<#escape x as -x><#noescape>${1}</#noescape></#escape>", squared),
                "naming convention", "camel", "#noescape");
        assertOutput(
                squared("<#escape x as -x><#noEscape>${1}</#noEscape></#escape>", squared),
                "1");

        // ---

        setConfiguration(new TestConfigurationBuilder()
                .tagSyntax(tagSyntax)
                .namingConvention(NamingConvention.LEGACY)
                .build());

        assertErrorContains(
                squared("<#if true>t<#elseIf false>f</#if>", squared),
                "naming convention", "legacy", "#elseIf");
        assertOutput(
                squared("<#if true>t<#elseif false>f</#if>", squared),
                "t");
        
        assertErrorContains(
                squared("<#noParse>${x}</#noParse>", squared),
                "naming convention", "legacy", "#noParse");
        assertOutput(
                squared("<#noparse>${x}</#noparse>", squared),
                "${x}");
        
        assertErrorContains(
                squared("<#escape x as -x><#noEscape>${1}</#noEscape></#escape>", squared),
                "naming convention", "legacy", "#noEscape");
        assertOutput(
                squared("<#escape x as -x><#noescape>${1}</#noescape></#escape>", squared),
                "1");
    }
    
    @Test
    public void inconsistentAutoDetectedNamingConvention() {
        assertErrorContains(
                "<#if x><#elseIf y><#elseif z></#if>",
                "naming convention", "camel");
        assertErrorContains(
                "<#if x><#elseif y><#elseIf z></#if>",
                "naming convention", "legacy");
        assertErrorContains(
                "<#if x><#elseIf y></#if><#noparse></#noparse>",
                "naming convention", "camel");
        assertErrorContains(
                "<#if x><#elseif y></#if><#noParse></#noParse>",
                "naming convention", "legacy");
        assertErrorContains(
                "<#if x><#elseif y><#elseIf z></#if>",
                "naming convention", "legacy");
        assertErrorContains(
                "<#escape x as x + 1><#noEscape></#noescape></#escape>",
                "naming convention", "camel");
        assertErrorContains(
                "<#escape x as x + 1><#noEscape></#noEscape><#noescape></#noescape></#escape>",
                "naming convention", "camel");
        assertErrorContains(
                "<#escape x as x + 1><#noescape></#noEscape></#escape>",
                "naming convention", "legacy");
        assertErrorContains(
                "<#escape x as x + 1><#noescape></#noescape><#noEscape></#noEscape></#escape>",
                "naming convention", "legacy");

        assertErrorContains("${x?upperCase?is_string}",
                "naming convention", "camel", "upperCase", "is_string");
        assertErrorContains("${x?upper_case?isString}",
                "naming convention", "legacy", "upper_case", "isString");

        assertErrorContains("<#setting outputEncoding='utf-8'>${x?is_string}",
                "naming convention", "camel", "outputEncoding", "is_string");
        assertErrorContains("<#setting output_encoding='utf-8'>${x?isString}",
                "naming convention", "legacy", "output_encoding", "isString");
        
        assertErrorContains("${x?isString}<#setting output_encoding='utf-8'>",
                "naming convention", "camel", "isString", "output_encoding");
        assertErrorContains("${x?is_string}<#setting outputEncoding='utf-8'>",
                "naming convention", "legacy", "is_string", "outputEncoding");
        
        assertErrorContains("${.outputEncoding}${x?is_string}",
                "naming convention", "camel", "outputEncoding", "is_string");
        assertErrorContains("${.output_encoding}${x?isString}",
                "naming convention", "legacy", "output_encoding", "isString");
        
        assertErrorContains("${x?upperCase}<#noparse></#noparse>",
                "naming convention", "camel", "upperCase", "noparse");
        assertErrorContains("${x?upper_case}<#noParse></#noParse>",
                "naming convention", "legacy", "upper_case", "noParse");
    }
    
    private interface NamePairAssertion {
        
        void assertPair(String name1, String name2);
        
    }
    
}
