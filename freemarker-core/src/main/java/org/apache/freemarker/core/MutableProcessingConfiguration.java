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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.freemarker.core.arithmetic.ArithmeticEngine;
import org.apache.freemarker.core.arithmetic.impl.BigDecimalArithmeticEngine;
import org.apache.freemarker.core.arithmetic.impl.ConservativeArithmeticEngine;
import org.apache.freemarker.core.model.ObjectWrapper;
import org.apache.freemarker.core.model.impl.DefaultObjectWrapper;
import org.apache.freemarker.core.model.impl.RestrictedObjectWrapper;
import org.apache.freemarker.core.outputformat.OutputFormat;
import org.apache.freemarker.core.outputformat.impl.HTMLOutputFormat;
import org.apache.freemarker.core.outputformat.impl.PlainTextOutputFormat;
import org.apache.freemarker.core.outputformat.impl.RTFOutputFormat;
import org.apache.freemarker.core.outputformat.impl.UndefinedOutputFormat;
import org.apache.freemarker.core.outputformat.impl.XMLOutputFormat;
import org.apache.freemarker.core.templateresolver.AndMatcher;
import org.apache.freemarker.core.templateresolver.ConditionalTemplateConfigurationFactory;
import org.apache.freemarker.core.templateresolver.FileNameGlobMatcher;
import org.apache.freemarker.core.templateresolver.FirstMatchTemplateConfigurationFactory;
import org.apache.freemarker.core.templateresolver.MergingTemplateConfigurationFactory;
import org.apache.freemarker.core.templateresolver.NotMatcher;
import org.apache.freemarker.core.templateresolver.OrMatcher;
import org.apache.freemarker.core.templateresolver.PathGlobMatcher;
import org.apache.freemarker.core.templateresolver.PathRegexMatcher;
import org.apache.freemarker.core.templateresolver.impl.DefaultTemplateNameFormat;
import org.apache.freemarker.core.templateresolver.impl.DefaultTemplateNameFormatFM2;
import org.apache.freemarker.core.util.FTLUtil;
import org.apache.freemarker.core.util.GenericParseException;
import org.apache.freemarker.core.util.OptInTemplateClassResolver;
import org.apache.freemarker.core.util._ClassUtil;
import org.apache.freemarker.core.util._CollectionUtil;
import org.apache.freemarker.core.util._KeyValuePair;
import org.apache.freemarker.core.util._NullArgumentException;
import org.apache.freemarker.core.util._SortedArraySet;
import org.apache.freemarker.core.util._StringUtil;
import org.apache.freemarker.core.valueformat.TemplateDateFormatFactory;
import org.apache.freemarker.core.valueformat.TemplateNumberFormatFactory;

/**
 * Extended by FreeMarker core classes (not by you) that support specifying {@link ProcessingConfiguration} setting
 * values. <b>New abstract methods may be added any time in future FreeMarker versions, so don't try to implement this
 * interface yourself!</b>
 */
public abstract class MutableProcessingConfiguration<SelfT extends MutableProcessingConfiguration<SelfT>>
        implements ProcessingConfiguration {
    public static final String NULL_VALUE = "null";
    public static final String DEFAULT_VALUE = "default";
    public static final String JVM_DEFAULT_VALUE = "JVM default";
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String LOCALE_KEY_SNAKE_CASE = "locale";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String LOCALE_KEY_CAMEL_CASE = "locale";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String LOCALE_KEY = LOCALE_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String NUMBER_FORMAT_KEY_SNAKE_CASE = "number_format";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String NUMBER_FORMAT_KEY_CAMEL_CASE = "numberFormat";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String NUMBER_FORMAT_KEY = NUMBER_FORMAT_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String CUSTOM_NUMBER_FORMATS_KEY_SNAKE_CASE = "custom_number_formats";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String CUSTOM_NUMBER_FORMATS_KEY_CAMEL_CASE = "customNumberFormats";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String CUSTOM_NUMBER_FORMATS_KEY = CUSTOM_NUMBER_FORMATS_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String TIME_FORMAT_KEY_SNAKE_CASE = "time_format";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String TIME_FORMAT_KEY_CAMEL_CASE = "timeFormat";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String TIME_FORMAT_KEY = TIME_FORMAT_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String DATE_FORMAT_KEY_SNAKE_CASE = "date_format";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String DATE_FORMAT_KEY_CAMEL_CASE = "dateFormat";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String DATE_FORMAT_KEY = DATE_FORMAT_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String CUSTOM_DATE_FORMATS_KEY_SNAKE_CASE = "custom_date_formats";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String CUSTOM_DATE_FORMATS_KEY_CAMEL_CASE = "customDateFormats";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String CUSTOM_DATE_FORMATS_KEY = CUSTOM_DATE_FORMATS_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String DATETIME_FORMAT_KEY_SNAKE_CASE = "datetime_format";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String DATETIME_FORMAT_KEY_CAMEL_CASE = "datetimeFormat";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String DATETIME_FORMAT_KEY = DATETIME_FORMAT_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String TIME_ZONE_KEY_SNAKE_CASE = "time_zone";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String TIME_ZONE_KEY_CAMEL_CASE = "timeZone";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String TIME_ZONE_KEY = TIME_ZONE_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String SQL_DATE_AND_TIME_TIME_ZONE_KEY_SNAKE_CASE = "sql_date_and_time_time_zone";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String SQL_DATE_AND_TIME_TIME_ZONE_KEY_CAMEL_CASE = "sqlDateAndTimeTimeZone";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String SQL_DATE_AND_TIME_TIME_ZONE_KEY = SQL_DATE_AND_TIME_TIME_ZONE_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String TEMPLATE_EXCEPTION_HANDLER_KEY_SNAKE_CASE = "template_exception_handler";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String TEMPLATE_EXCEPTION_HANDLER_KEY_CAMEL_CASE = "templateExceptionHandler";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String TEMPLATE_EXCEPTION_HANDLER_KEY = TEMPLATE_EXCEPTION_HANDLER_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String ARITHMETIC_ENGINE_KEY_SNAKE_CASE = "arithmetic_engine";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String ARITHMETIC_ENGINE_KEY_CAMEL_CASE = "arithmeticEngine";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String ARITHMETIC_ENGINE_KEY = ARITHMETIC_ENGINE_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String OBJECT_WRAPPER_KEY_SNAKE_CASE = "object_wrapper";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String OBJECT_WRAPPER_KEY_CAMEL_CASE = "objectWrapper";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String OBJECT_WRAPPER_KEY = OBJECT_WRAPPER_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String BOOLEAN_FORMAT_KEY_SNAKE_CASE = "boolean_format";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String BOOLEAN_FORMAT_KEY_CAMEL_CASE = "booleanFormat";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String BOOLEAN_FORMAT_KEY = BOOLEAN_FORMAT_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String OUTPUT_ENCODING_KEY_SNAKE_CASE = "output_encoding";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String OUTPUT_ENCODING_KEY_CAMEL_CASE = "outputEncoding";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String OUTPUT_ENCODING_KEY = OUTPUT_ENCODING_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String URL_ESCAPING_CHARSET_KEY_SNAKE_CASE = "url_escaping_charset";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String URL_ESCAPING_CHARSET_KEY_CAMEL_CASE = "urlEscapingCharset";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String URL_ESCAPING_CHARSET_KEY = URL_ESCAPING_CHARSET_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String AUTO_FLUSH_KEY_SNAKE_CASE = "auto_flush";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String AUTO_FLUSH_KEY_CAMEL_CASE = "autoFlush";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. @since 2.3.17 */
    public static final String AUTO_FLUSH_KEY = AUTO_FLUSH_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String NEW_BUILTIN_CLASS_RESOLVER_KEY_SNAKE_CASE = "new_builtin_class_resolver";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String NEW_BUILTIN_CLASS_RESOLVER_KEY_CAMEL_CASE = "newBuiltinClassResolver";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. @since 2.3.17 */
    public static final String NEW_BUILTIN_CLASS_RESOLVER_KEY = NEW_BUILTIN_CLASS_RESOLVER_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String SHOW_ERROR_TIPS_KEY_SNAKE_CASE = "show_error_tips";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String SHOW_ERROR_TIPS_KEY_CAMEL_CASE = "showErrorTips";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. @since 2.3.21 */
    public static final String SHOW_ERROR_TIPS_KEY = SHOW_ERROR_TIPS_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String API_BUILTIN_ENABLED_KEY_SNAKE_CASE = "api_builtin_enabled";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String API_BUILTIN_ENABLED_KEY_CAMEL_CASE = "apiBuiltinEnabled";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. @since 2.3.22 */
    public static final String API_BUILTIN_ENABLED_KEY = API_BUILTIN_ENABLED_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.23 */
    public static final String LOG_TEMPLATE_EXCEPTIONS_KEY_SNAKE_CASE = "log_template_exceptions";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.23 */
    public static final String LOG_TEMPLATE_EXCEPTIONS_KEY_CAMEL_CASE = "logTemplateExceptions";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. @since 2.3.22 */
    public static final String LOG_TEMPLATE_EXCEPTIONS_KEY = LOG_TEMPLATE_EXCEPTIONS_KEY_SNAKE_CASE;

    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.25 */
    public static final String LAZY_IMPORTS_KEY_SNAKE_CASE = "lazy_imports";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.25 */
    public static final String LAZY_IMPORTS_KEY_CAMEL_CASE = "lazyImports";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String LAZY_IMPORTS_KEY = LAZY_IMPORTS_KEY_SNAKE_CASE;

    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.25 */
    public static final String LAZY_AUTO_IMPORTS_KEY_SNAKE_CASE = "lazy_auto_imports";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.25 */
    public static final String LAZY_AUTO_IMPORTS_KEY_CAMEL_CASE = "lazyAutoImports";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String LAZY_AUTO_IMPORTS_KEY = LAZY_AUTO_IMPORTS_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.25 */
    public static final String AUTO_IMPORT_KEY_SNAKE_CASE = "auto_import";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.25 */
    public static final String AUTO_IMPORT_KEY_CAMEL_CASE = "autoImport";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String AUTO_IMPORT_KEY = AUTO_IMPORT_KEY_SNAKE_CASE;
    
    /** Legacy, snake case ({@code like_this}) variation of the setting name. @since 2.3.25 */
    public static final String AUTO_INCLUDE_KEY_SNAKE_CASE = "auto_include";
    /** Modern, camel case ({@code likeThis}) variation of the setting name. @since 2.3.25 */
    public static final String AUTO_INCLUDE_KEY_CAMEL_CASE = "autoInclude";
    /** Alias to the {@code ..._SNAKE_CASE} variation due to backward compatibility constraints. */
    public static final String AUTO_INCLUDE_KEY = AUTO_INCLUDE_KEY_SNAKE_CASE;
    
    private static final String[] SETTING_NAMES_SNAKE_CASE = new String[] {
        // Must be sorted alphabetically!
        API_BUILTIN_ENABLED_KEY_SNAKE_CASE,
        ARITHMETIC_ENGINE_KEY_SNAKE_CASE,
        AUTO_FLUSH_KEY_SNAKE_CASE,
        AUTO_IMPORT_KEY_SNAKE_CASE,
        AUTO_INCLUDE_KEY_SNAKE_CASE,
        BOOLEAN_FORMAT_KEY_SNAKE_CASE,
        CUSTOM_DATE_FORMATS_KEY_SNAKE_CASE,
        CUSTOM_NUMBER_FORMATS_KEY_SNAKE_CASE,
        DATE_FORMAT_KEY_SNAKE_CASE,
        DATETIME_FORMAT_KEY_SNAKE_CASE,
        LAZY_AUTO_IMPORTS_KEY_SNAKE_CASE,
        LAZY_IMPORTS_KEY_SNAKE_CASE,
        LOCALE_KEY_SNAKE_CASE,
        LOG_TEMPLATE_EXCEPTIONS_KEY_SNAKE_CASE,
        NEW_BUILTIN_CLASS_RESOLVER_KEY_SNAKE_CASE,
        NUMBER_FORMAT_KEY_SNAKE_CASE,
        OBJECT_WRAPPER_KEY_SNAKE_CASE,
        OUTPUT_ENCODING_KEY_SNAKE_CASE,
        SHOW_ERROR_TIPS_KEY_SNAKE_CASE,
        SQL_DATE_AND_TIME_TIME_ZONE_KEY_SNAKE_CASE,
        TEMPLATE_EXCEPTION_HANDLER_KEY_SNAKE_CASE,
        TIME_FORMAT_KEY_SNAKE_CASE,
        TIME_ZONE_KEY_SNAKE_CASE,
        URL_ESCAPING_CHARSET_KEY_SNAKE_CASE
    };
    
    private static final String[] SETTING_NAMES_CAMEL_CASE = new String[] {
        // Must be sorted alphabetically!
        API_BUILTIN_ENABLED_KEY_CAMEL_CASE,
        ARITHMETIC_ENGINE_KEY_CAMEL_CASE,
        AUTO_FLUSH_KEY_CAMEL_CASE,
        AUTO_IMPORT_KEY_CAMEL_CASE,
        AUTO_INCLUDE_KEY_CAMEL_CASE,
        BOOLEAN_FORMAT_KEY_CAMEL_CASE,
        CUSTOM_DATE_FORMATS_KEY_CAMEL_CASE,
        CUSTOM_NUMBER_FORMATS_KEY_CAMEL_CASE,
        DATE_FORMAT_KEY_CAMEL_CASE,
        DATETIME_FORMAT_KEY_CAMEL_CASE,
        LAZY_AUTO_IMPORTS_KEY_CAMEL_CASE,
        LAZY_IMPORTS_KEY_CAMEL_CASE,
        LOCALE_KEY_CAMEL_CASE,
        LOG_TEMPLATE_EXCEPTIONS_KEY_CAMEL_CASE,
        NEW_BUILTIN_CLASS_RESOLVER_KEY_CAMEL_CASE,
        NUMBER_FORMAT_KEY_CAMEL_CASE,
        OBJECT_WRAPPER_KEY_CAMEL_CASE,
        OUTPUT_ENCODING_KEY_CAMEL_CASE,
        SHOW_ERROR_TIPS_KEY_CAMEL_CASE,
        SQL_DATE_AND_TIME_TIME_ZONE_KEY_CAMEL_CASE,
        TEMPLATE_EXCEPTION_HANDLER_KEY_CAMEL_CASE,
        TIME_FORMAT_KEY_CAMEL_CASE,
        TIME_ZONE_KEY_CAMEL_CASE,
        URL_ESCAPING_CHARSET_KEY_CAMEL_CASE
    };

    private Locale locale;
    private String numberFormat;
    private String timeFormat;
    private String dateFormat;
    private String dateTimeFormat;
    private TimeZone timeZone;
    private TimeZone sqlDateAndTimeTimeZone;
    private boolean sqlDateAndTimeTimeZoneSet;
    private String booleanFormat;
    private TemplateExceptionHandler templateExceptionHandler;
    private ArithmeticEngine arithmeticEngine;
    private ObjectWrapper objectWrapper;
    private Charset outputEncoding;
    private boolean outputEncodingSet;
    private Charset urlEscapingCharset;
    private boolean urlEscapingCharsetSet;
    private Boolean autoFlush;
    private TemplateClassResolver newBuiltinClassResolver;
    private Boolean showErrorTips;
    private Boolean apiBuiltinEnabled;
    private Boolean logTemplateExceptions;
    private Map<String, TemplateDateFormatFactory> customDateFormats;
    private Map<String, TemplateNumberFormatFactory> customNumberFormats;
    private LinkedHashMap<String, String> autoImports;
    private ArrayList<String> autoIncludes;
    private Boolean lazyImports;
    private Boolean lazyAutoImports;
    private boolean lazyAutoImportsSet;
    private Map<Object, Object> customAttributes;

    /**
     * Creates a new instance. Normally you do not need to use this constructor,
     * as you don't use <code>MutableProcessingConfiguration</code> directly, but its subclasses.
     */
    protected MutableProcessingConfiguration() {
        // Empty
    }

    @Override
    public Locale getLocale() {
         return isLocaleSet() ? locale : getDefaultLocale();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract Locale getDefaultLocale();

    @Override
    public boolean isLocaleSet() {
        return locale != null;
    }

    /**
     * Setter pair of {@link ProcessingConfiguration#getLocale()}.
     */
    public void setLocale(Locale locale) {
        _NullArgumentException.check("locale", locale);
        this.locale = locale;
    }

    /**
     * Fluent API equivalent of {@link #setLocale(Locale)}
     */
    public SelfT locale(Locale value) {
        setLocale(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetLocale() {
        locale = null;
    }

    /**
     * Setter pair of {@link ProcessingConfiguration#getTimeZone()}.
     */
    public void setTimeZone(TimeZone timeZone) {
        _NullArgumentException.check("timeZone", timeZone);
        this.timeZone = timeZone;
    }

    /**
     * Fluent API equivalent of {@link #setTimeZone(TimeZone)}
     */
    public SelfT timeZone(TimeZone value) {
        setTimeZone(value);
        return self();
    }
    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetTimeZone() {
        this.timeZone = null;
    }

    @Override
    public TimeZone getTimeZone() {
         return isTimeZoneSet() ? timeZone : getDefaultTimeZone();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract TimeZone getDefaultTimeZone();

    @Override
    public boolean isTimeZoneSet() {
        return timeZone != null;
    }
    
    /**
     * Setter pair of {@link ProcessingConfiguration#getSQLDateAndTimeTimeZone()}.
     */
    public void setSQLDateAndTimeTimeZone(TimeZone tz) {
        sqlDateAndTimeTimeZone = tz;
        sqlDateAndTimeTimeZoneSet = true;
    }

    /**
     * Fluent API equivalent of {@link #setSQLDateAndTimeTimeZone(TimeZone)}
     */
    public SelfT sqlDateAndTimeTimeZone(TimeZone value) {
        setSQLDateAndTimeTimeZone(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetSQLDateAndTimeTimeZone() {
        sqlDateAndTimeTimeZone = null;
        sqlDateAndTimeTimeZoneSet = false;
    }

    @Override
    public TimeZone getSQLDateAndTimeTimeZone() {
        return sqlDateAndTimeTimeZoneSet
                ? sqlDateAndTimeTimeZone
                : getDefaultSQLDateAndTimeTimeZone();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract TimeZone getDefaultSQLDateAndTimeTimeZone();

    @Override
    public boolean isSQLDateAndTimeTimeZoneSet() {
        return sqlDateAndTimeTimeZoneSet;
    }

    /**
     * Setter pair of {@link #getNumberFormat()}
     */
    public void setNumberFormat(String numberFormat) {
        _NullArgumentException.check("numberFormat", numberFormat);
        this.numberFormat = numberFormat;
    }

    /**
     * Fluent API equivalent of {@link #setNumberFormat(String)}
     */
    public SelfT numberFormat(String value) {
        setNumberFormat(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetNumberFormat() {
        numberFormat = null;
    }

    @Override
    public String getNumberFormat() {
         return isNumberFormatSet() ? numberFormat : getDefaultNumberFormat();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract String getDefaultNumberFormat();

    @Override
    public boolean isNumberFormatSet() {
        return numberFormat != null;
    }
    
    @Override
    public Map<String, TemplateNumberFormatFactory> getCustomNumberFormats() {
         return isCustomNumberFormatsSet() ? customNumberFormats : getDefaultCustomNumberFormats();
    }

    protected abstract Map<String, TemplateNumberFormatFactory> getDefaultCustomNumberFormats();

    /**
     * Setter pair of {@link #getCustomNumberFormats()}. Note that custom number formats are get through
     * {@link #getCustomNumberFormat(String)}, not directly though this {@link Map}, so number formats from
     * {@link ProcessingConfiguration}-s on less specific levels are inherited without you copying them into this
     * {@link Map}.
     *
     * @param customNumberFormats
     *      Not {@code null}.
     */
    public void setCustomNumberFormats(Map<String, TemplateNumberFormatFactory> customNumberFormats) {
        _NullArgumentException.check("customNumberFormats", customNumberFormats);
        validateFormatNames(customNumberFormats.keySet());
        this.customNumberFormats = customNumberFormats;
    }

    /**
     * Fluent API equivalent of {@link #setCustomNumberFormats(Map)}
     */
    public SelfT customNumberFormats(Map<String, TemplateNumberFormatFactory> value) {
        setCustomNumberFormats(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetCustomNumberFormats() {
        customNumberFormats = null;
    }

    private void validateFormatNames(Set<String> keySet) {
        for (String name : keySet) {
            if (name.length() == 0) {
                throw new IllegalArgumentException("Format names can't be 0 length");
            }
            char firstChar = name.charAt(0);
            if (firstChar == '@') {
                throw new IllegalArgumentException(
                        "Format names can't start with '@'. '@' is only used when referring to them from format "
                        + "strings. In: " + name);
            }
            if (!Character.isLetter(firstChar)) {
                throw new IllegalArgumentException("Format name must start with letter: " + name);
            }
            for (int i = 1; i < name.length(); i++) {
                // Note that we deliberately don't allow "_" here.
                if (!Character.isLetterOrDigit(name.charAt(i))) {
                    throw new IllegalArgumentException("Format name can only contain letters and digits: " + name);
                }
            }
        }
    }

    @Override
    public boolean isCustomNumberFormatsSet() {
        return customNumberFormats != null;
    }

    @Override
    public TemplateNumberFormatFactory getCustomNumberFormat(String name) {
        TemplateNumberFormatFactory r;
        if (customNumberFormats != null) {
            r = customNumberFormats.get(name);
            if (r != null) {
                return r;
            }
        }
        return getDefaultCustomNumberFormat(name);
    }

    protected abstract TemplateNumberFormatFactory getDefaultCustomNumberFormat(String name);

    /**
     * Setter pair of {@link #getBooleanFormat()}.
     */
    public void setBooleanFormat(String booleanFormat) {
        _NullArgumentException.check("booleanFormat", booleanFormat);
        
        int commaIdx = booleanFormat.indexOf(',');
        if (commaIdx == -1) {
            throw new IllegalArgumentException(
                    "Setting value must be string that contains two comma-separated values for true and false, " +
                    "respectively.");
        }
        
        this.booleanFormat = booleanFormat; 
    }

    /**
     * Fluent API equivalent of {@link #setBooleanFormat(String)}
     */
    public SelfT booleanFormat(String value) {
        setBooleanFormat(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetBooleanFormat() {
        booleanFormat = null;
    }
    
    @Override
    public String getBooleanFormat() {
         return isBooleanFormatSet() ? booleanFormat : getDefaultBooleanFormat();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract String getDefaultBooleanFormat();

    @Override
    public boolean isBooleanFormatSet() {
        return booleanFormat != null;
    }

    /**
     * Setter pair of {@link #getTimeFormat()}
     */
    public void setTimeFormat(String timeFormat) {
        _NullArgumentException.check("timeFormat", timeFormat);
        this.timeFormat = timeFormat;
    }

    /**
     * Fluent API equivalent of {@link #setTimeFormat(String)}
     */
    public SelfT timeFormat(String value) {
        setTimeFormat(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetTimeFormat() {
        timeFormat = null;
    }

    @Override
    public String getTimeFormat() {
         return isTimeFormatSet() ? timeFormat : getDefaultTimeFormat();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract String getDefaultTimeFormat();

    @Override
    public boolean isTimeFormatSet() {
        return timeFormat != null;
    }

    /**
     * Setter pair of {@link #getDateFormat()}.
     */
    public void setDateFormat(String dateFormat) {
        _NullArgumentException.check("dateFormat", dateFormat);
        this.dateFormat = dateFormat;
    }

    /**
     * Fluent API equivalent of {@link #setDateFormat(String)}
     */
    public SelfT dateFormat(String value) {
        setDateFormat(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetDateFormat() {
        dateFormat = null;
    }

    @Override
    public String getDateFormat() {
         return isDateFormatSet() ? dateFormat : getDefaultDateFormat();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract String getDefaultDateFormat();

    @Override
    public boolean isDateFormatSet() {
        return dateFormat != null;
    }

    /**
     * Setter pair of {@link #getDateTimeFormat()}
     */
    public void setDateTimeFormat(String dateTimeFormat) {
        _NullArgumentException.check("dateTimeFormat", dateTimeFormat);
        this.dateTimeFormat = dateTimeFormat;
    }

    /**
     * Fluent API equivalent of {@link #setDateTimeFormat(String)}
     */
    public SelfT dateTimeFormat(String value) {
        setDateTimeFormat(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetDateTimeFormat() {
        this.dateTimeFormat = null;
    }

    @Override
    public String getDateTimeFormat() {
         return isDateTimeFormatSet() ? dateTimeFormat : getDefaultDateTimeFormat();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract String getDefaultDateTimeFormat();

    @Override
    public boolean isDateTimeFormatSet() {
        return dateTimeFormat != null;
    }

    @Override
    public Map<String, TemplateDateFormatFactory> getCustomDateFormats() {
         return isCustomDateFormatsSet() ? customDateFormats : getDefaultCustomDateFormats();
    }

    protected abstract Map<String, TemplateDateFormatFactory> getDefaultCustomDateFormats();

    /**
     * Setter pair of {@link #getCustomDateFormat(String)}.
     */
    public void setCustomDateFormats(Map<String, TemplateDateFormatFactory> customDateFormats) {
        _NullArgumentException.check("customDateFormats", customDateFormats);
        validateFormatNames(customDateFormats.keySet());
        this.customDateFormats = customDateFormats;
    }

    /**
     * Fluent API equivalent of {@link #setCustomDateFormats(Map)}
     */
    public SelfT customDateFormats(Map<String, TemplateDateFormatFactory> value) {
        setCustomDateFormats(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetCustomDateFormats() {
        this.customDateFormats = null;
    }

    @Override
    public boolean isCustomDateFormatsSet() {
        return customDateFormats != null;
    }

    @Override
    public TemplateDateFormatFactory getCustomDateFormat(String name) {
        TemplateDateFormatFactory r;
        if (customDateFormats != null) {
            r = customDateFormats.get(name);
            if (r != null) {
                return r;
            }
        }
        return getDefaultCustomDateFormat(name);
    }

    protected abstract TemplateDateFormatFactory getDefaultCustomDateFormat(String name);

    /**
     * Setter pair of {@link #getTemplateExceptionHandler()}
     */
    public void setTemplateExceptionHandler(TemplateExceptionHandler templateExceptionHandler) {
        _NullArgumentException.check("templateExceptionHandler", templateExceptionHandler);
        this.templateExceptionHandler = templateExceptionHandler;
    }

    /**
     * Fluent API equivalent of {@link #setTemplateExceptionHandler(TemplateExceptionHandler)}
     */
    public SelfT templateExceptionHandler(TemplateExceptionHandler value) {
        setTemplateExceptionHandler(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetTemplateExceptionHandler() {
        templateExceptionHandler = null;
    }

    @Override
    public TemplateExceptionHandler getTemplateExceptionHandler() {
         return isTemplateExceptionHandlerSet()
                ? templateExceptionHandler : getDefaultTemplateExceptionHandler();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract TemplateExceptionHandler getDefaultTemplateExceptionHandler();

    @Override
    public boolean isTemplateExceptionHandlerSet() {
        return templateExceptionHandler != null;
    }

    /**
     * Setter pair of {@link #getArithmeticEngine()}
     */
    public void setArithmeticEngine(ArithmeticEngine arithmeticEngine) {
        _NullArgumentException.check("arithmeticEngine", arithmeticEngine);
        this.arithmeticEngine = arithmeticEngine;
    }

    /**
     * Fluent API equivalent of {@link #setArithmeticEngine(ArithmeticEngine)}
     */
    public SelfT arithmeticEngine(ArithmeticEngine value) {
        setArithmeticEngine(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetArithmeticEngine() {
        this.arithmeticEngine = null;
    }

    @Override
    public ArithmeticEngine getArithmeticEngine() {
         return isArithmeticEngineSet() ? arithmeticEngine : getDefaultArithmeticEngine();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract ArithmeticEngine getDefaultArithmeticEngine();

    @Override
    public boolean isArithmeticEngineSet() {
        return arithmeticEngine != null;
    }

    /**
     * Setter pair of {@link #getObjectWrapper()}
     */
    public void setObjectWrapper(ObjectWrapper objectWrapper) {
        _NullArgumentException.check("objectWrapper", objectWrapper);
        this.objectWrapper = objectWrapper;
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetObjectWrapper() {
        objectWrapper = null;
    }

    /**
     * Fluent API equivalent of {@link #setObjectWrapper(ObjectWrapper)}
     */
    public SelfT objectWrapper(ObjectWrapper value) {
        setObjectWrapper(value);
        return self();
    }

    @Override
    public ObjectWrapper getObjectWrapper() {
         return isObjectWrapperSet()
                ? objectWrapper : getDefaultObjectWrapper();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract ObjectWrapper getDefaultObjectWrapper();

    @Override
    public boolean isObjectWrapperSet() {
        return objectWrapper != null;
    }

    /**
     * The setter pair of {@link #getOutputEncoding()}
     */
    public void setOutputEncoding(Charset outputEncoding) {
        this.outputEncoding = outputEncoding;
        outputEncodingSet = true;
    }

    /**
     * Fluent API equivalent of {@link #setOutputEncoding(Charset)}
     */
    public SelfT outputEncoding(Charset value) {
        setOutputEncoding(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetOutputEncoding() {
        this.outputEncoding = null;
        outputEncodingSet = false;
    }

    @Override
    public Charset getOutputEncoding() {
        return isOutputEncodingSet()
                ? outputEncoding
                : getDefaultOutputEncoding();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract Charset getDefaultOutputEncoding();

    @Override
    public boolean isOutputEncodingSet() {
        return outputEncodingSet;
    }

    /**
     * The setter pair of {@link #getURLEscapingCharset()}.
     */
    public void setURLEscapingCharset(Charset urlEscapingCharset) {
        this.urlEscapingCharset = urlEscapingCharset;
        urlEscapingCharsetSet = true;
    }

    /**
     * Fluent API equivalent of {@link #setURLEscapingCharset(Charset)}
     */
    public SelfT urlEscapingCharset(Charset value) {
        setURLEscapingCharset(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetURLEscapingCharset() {
        this.urlEscapingCharset = null;
        urlEscapingCharsetSet = false;
    }

    @Override
    public Charset getURLEscapingCharset() {
        return isURLEscapingCharsetSet() ? urlEscapingCharset : getDefaultURLEscapingCharset();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract Charset getDefaultURLEscapingCharset();

    @Override
    public boolean isURLEscapingCharsetSet() {
        return urlEscapingCharsetSet;
    }

    /**
     * Setter pair of {@link #getNewBuiltinClassResolver()}
     */
    public void setNewBuiltinClassResolver(TemplateClassResolver newBuiltinClassResolver) {
        _NullArgumentException.check("newBuiltinClassResolver", newBuiltinClassResolver);
        this.newBuiltinClassResolver = newBuiltinClassResolver;
    }

    /**
     * Fluent API equivalent of {@link #setNewBuiltinClassResolver(TemplateClassResolver)}
     */
    public SelfT newBuiltinClassResolver(TemplateClassResolver value) {
        setNewBuiltinClassResolver(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetNewBuiltinClassResolver() {
        this.newBuiltinClassResolver = null;
    }

    @Override
    public TemplateClassResolver getNewBuiltinClassResolver() {
         return isNewBuiltinClassResolverSet()
                ? newBuiltinClassResolver : getDefaultNewBuiltinClassResolver();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract TemplateClassResolver getDefaultNewBuiltinClassResolver();

    @Override
    public boolean isNewBuiltinClassResolverSet() {
        return newBuiltinClassResolver != null;
    }

    /**
     * Setter pair of {@link #getAutoFlush()}
     */
    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }

    /**
     * Fluent API equivalent of {@link #setAutoFlush(boolean)}
     */
    public SelfT autoFlush(boolean value) {
        setAutoFlush(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetAutoFlush() {
        this.autoFlush = null;
    }

    @Override
    public boolean getAutoFlush() {
         return isAutoFlushSet() ? autoFlush : getDefaultAutoFlush();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract boolean getDefaultAutoFlush();

    @Override
    public boolean isAutoFlushSet() {
        return autoFlush != null;
    }

    /**
     * Setter pair of {@link #getShowErrorTips()}
     */
    public void setShowErrorTips(boolean showTips) {
        showErrorTips = showTips;
    }

    /**
     * Fluent API equivalent of {@link #setShowErrorTips(boolean)}
     */
    public SelfT showErrorTips(boolean value) {
        setShowErrorTips(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetShowErrorTips() {
        showErrorTips = null;
    }

    @Override
    public boolean getShowErrorTips() {
         return isShowErrorTipsSet() ? showErrorTips : getDefaultShowErrorTips();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract boolean getDefaultShowErrorTips();

    @Override
    public boolean isShowErrorTipsSet() {
        return showErrorTips != null;
    }

    /**
     * Setter pair of {@link #getAPIBuiltinEnabled()}
     */
    public void setAPIBuiltinEnabled(boolean value) {
        apiBuiltinEnabled = Boolean.valueOf(value);
    }

    /**
     * Fluent API equivalent of {@link #setAPIBuiltinEnabled(boolean)}
     */
    public SelfT apiBuiltinEnabled(boolean value) {
        setAPIBuiltinEnabled(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetAPIBuiltinEnabled() {
        apiBuiltinEnabled = null;
    }

    @Override
    public boolean getAPIBuiltinEnabled() {
         return isAPIBuiltinEnabledSet() ? apiBuiltinEnabled : getDefaultAPIBuiltinEnabled();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract boolean getDefaultAPIBuiltinEnabled();

    @Override
    public boolean isAPIBuiltinEnabledSet() {
        return apiBuiltinEnabled != null;
    }

    @Override
    public boolean getLogTemplateExceptions() {
         return isLogTemplateExceptionsSet() ? logTemplateExceptions : getDefaultLogTemplateExceptions();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract boolean getDefaultLogTemplateExceptions();

    @Override
    public boolean isLogTemplateExceptionsSet() {
        return logTemplateExceptions != null;
    }

    /**
     * Setter pair of {@link #getLogTemplateExceptions()}
     */
    public void setLogTemplateExceptions(boolean value) {
        logTemplateExceptions = value;
    }

    /**
     * Fluent API equivalent of {@link #setLogTemplateExceptions(boolean)}
     */
    public SelfT logTemplateExceptions(boolean value) {
        setLogTemplateExceptions(value);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetLogTemplateExceptions() {
        logTemplateExceptions = null;
    }

    @Override
    public boolean getLazyImports() {
         return isLazyImportsSet() ? lazyImports : getDefaultLazyImports();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract boolean getDefaultLazyImports();

    /**
     * Setter pair of {@link #getLazyImports()}
     */
    public void setLazyImports(boolean lazyImports) {
        this.lazyImports = lazyImports;
    }

    /**
     * Fluent API equivalent of {@link #setLazyImports(boolean)}
     */
    public SelfT lazyImports(boolean lazyImports) {
        setLazyImports(lazyImports);
        return  self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetLazyImports() {
        this.lazyImports = null;
    }

    @Override
    public boolean isLazyImportsSet() {
        return lazyImports != null;
    }

    @Override
    public Boolean getLazyAutoImports() {
        return isLazyAutoImportsSet() ? lazyAutoImports : getDefaultLazyAutoImports();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract Boolean getDefaultLazyAutoImports();

    /**
     * Setter pair of {@link #getLazyAutoImports()}
     */
    public void setLazyAutoImports(Boolean lazyAutoImports) {
        this.lazyAutoImports = lazyAutoImports;
        lazyAutoImportsSet = true;
    }

    /**
     * Fluent API equivalent of {@link #setLazyAutoImports(Boolean)}
     */
    public SelfT lazyAutoImports(Boolean lazyAutoImports) {
        setLazyAutoImports(lazyAutoImports);
        return self();
    }

    /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetLazyAutoImports() {
        lazyAutoImports = null;
        lazyAutoImportsSet = false;
    }

    @Override
    public boolean isLazyAutoImportsSet() {
        return lazyAutoImportsSet;
    }
    
    /**
     * Adds the auto-import at the end of {@link #getAutoImports()}. If an auto-import with the same namespace variable
     * name already exists in the {@link Map}, it will be removed before the new one is added.
     */
    public void addAutoImport(String namespaceVarName, String templateName) {
        if (autoImports == null) {
            initAutoImportsMap();
        } else {
            // This was a List earlier, so re-inserted items must go to the end, hence we remove() before put().
            autoImports.remove(namespaceVarName);
        }
        autoImports.put(namespaceVarName, templateName);
    }

    private void initAutoImportsMap() {
        autoImports = new LinkedHashMap<>(4);
    }
    
    /**
     * Removes an auto-import from {@link #getAutoImports()} (but doesn't affect auto-imports inherited from another
     * {@link ParsingConfiguration}). Does nothing if the auto-import doesn't exist.
     */
    public void removeAutoImport(String namespaceVarName) {
        if (autoImports != null) {
            autoImports.remove(namespaceVarName);
        }
    }
    
    /**
     * Setter pair of {@link #getAutoImports()}.
     * 
     * @param map
     *            Maps the namespace variable names to the template names; not {@code null}. The content of the
     *            {@link Map} is copied into another {@link Map}, to avoid aliasing problems.
     */
    public void setAutoImports(Map map) {
        _NullArgumentException.check("map", map);
        
        if (autoImports != null) {
            autoImports.clear();
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) map).entrySet()) {
            Object key = entry.getKey();
            if (!(key instanceof String)) {
                throw new IllegalArgumentException(
                        "Key in Map wasn't a String, but a(n) " + key.getClass().getName() + ".");
            }

            Object value = entry.getValue();
            if (!(value instanceof String)) {
                throw new IllegalArgumentException(
                        "Value in Map wasn't a String, but a(n) " + value.getClass().getName() + ".");
            }

            addAutoImport((String) key, (String) value);
        }
    }

    /**
     * Fluent API equivalent of {@link #setAutoImports(Map)}
     */
    public SelfT autoImports(Map map) {
        setAutoImports(map);
        return self();
    }

     /**
     * Resets the setting value as if it was never set (but it doesn't affect the value inherited from another
     * {@link ProcessingConfiguration}).
     */
    public void unsetAutoImports() {
        autoImports = null;
    }

    @Override
    public Map<String, String> getAutoImports() {
         return isAutoImportsSet() ? autoImports : getDefaultAutoImports();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract Map<String,String> getDefaultAutoImports();

    @Override
    public boolean isAutoImportsSet() {
        return autoImports != null;
    }

    /**
     * Adds an auto-include to {@link #getAutoIncludes()}. If the template name is already in the {@link List}, then it
     * will be removed before it's added again (so in effect it's moved to the end of the {@link List}).
     */
    public void addAutoInclude(String templateName) {
        if (autoIncludes == null) {
            initAutoIncludesList();
        } else {
            autoIncludes.remove(templateName);
        }
        autoIncludes.add(templateName);
    }

    private void initAutoIncludesList() {
        autoIncludes = new ArrayList<>(4);
    }
    
    /**
     * Setter pair of {@link #getAutoIncludes()}
     *
     * @param templateNames Not {@code null}. The {@link List} will be copied to avoid aliasing problems.
     */
    public void setAutoIncludes(List templateNames) {
        _NullArgumentException.check("templateNames", templateNames);
        if (autoIncludes != null) {
            autoIncludes.clear();
        }
        for (Object templateName : templateNames) {
            if (!(templateName instanceof String)) {
                throw new IllegalArgumentException("List items must be String-s.");
            }
            addAutoInclude((String) templateName);
        }
    }

    /**
     * Fluent API equivalent of {@link #setAutoIncludes(List)}
     */
    public SelfT autoIncludes(List templateNames) {
        setAutoIncludes(templateNames);
        return self();
    }

    @Override
    public List<String> getAutoIncludes() {
         return isAutoIncludesSet() ? autoIncludes : getDefaultAutoIncludes();
    }

    /**
     * Returns the value the getter method returns when the setting is not set (possibly by inheriting the setting value
     * from another {@link ProcessingConfiguration}), or throws {@link SettingValueNotSetException}.
     */
    protected abstract List<String> getDefaultAutoIncludes();

    @Override
    public boolean isAutoIncludesSet() {
        return autoIncludes != null;
    }
    
    /**
     * Removes the auto-include from the {@link #getAutoIncludes()} {@link List} (but it doesn't affect the
     * {@link List}-s inherited from other {@link ProcessingConfiguration}-s). Does nothing if the template is not
     * in the {@link List}.
     */
    public void removeAutoInclude(String templateName) {
        // "synchronized" is removed from the API as it's not safe to set anything after publishing the Configuration
        synchronized (this) {
            if (autoIncludes != null) {
                autoIncludes.remove(templateName);
            }
        }
    }
    
    private static final String ALLOWED_CLASSES = "allowed_classes";
    private static final String TRUSTED_TEMPLATES = "trusted_templates";
    
    /**
     * Sets a FreeMarker setting by a name and string value. If you can configure FreeMarker directly with Java (or
     * other programming language), you should use the dedicated setter methods instead (like
     * {@link #setObjectWrapper(ObjectWrapper)}. This meant to be used only when you get settings from somewhere
     * as {@link String}-{@link String} name-value pairs (typically, as a {@link Properties} object). Below you find an
     * overview of the settings available.
     * 
     * <p>Note: As of FreeMarker 2.3.23, setting names can be written in camel case too. For example, instead of
     * {@code date_format} you can also use {@code dateFormat}. It's likely that camel case will become to the
     * recommended convention in the future.
     * 
     * <p>The list of settings commonly supported in all {@link MutableProcessingConfiguration} subclasses:
     * <ul>
     *   <li><p>{@code "locale"}:
     *       See {@link #setLocale(Locale)}.
     *       <br>String value: local codes with the usual format in Java, such as {@code "en_US"}, or
     *       "JVM default" (ignoring case) to use the default locale of the Java environment.
     *
     *   <li><p>{@code "custom_number_formats"}: See {@link #setCustomNumberFormats(Map)}.
     *   <br>String value: Interpreted as an <a href="#fm_obe">object builder expression</a>.
     *   <br>Example: <code>{ "hex": com.example.HexTemplateNumberFormatFactory,
     *   "gps": com.example.GPSTemplateNumberFormatFactory }</code>
     *
     *   <li><p>{@code "custom_date_formats"}: See {@link #setCustomDateFormats(Map)}.
     *   <br>String value: Interpreted as an <a href="#fm_obe">object builder expression</a>.
     *   <br>Example: <code>{ "trade": com.example.TradeTemplateDateFormatFactory,
     *   "log": com.example.LogTemplateDateFormatFactory }</code>
     *       
     *   <li><p>{@code "template_exception_handler"}:
     *       See {@link #setTemplateExceptionHandler(TemplateExceptionHandler)}.
     *       <br>String value: If the value contains dot, then it's interpreted as an <a href="#fm_obe">object builder
     *       expression</a>.
     *       If the value does not contain dot, then it must be one of these predefined values (case insensitive):
     *       {@code "rethrow"} (means {@link TemplateExceptionHandler#RETHROW_HANDLER}),
     *       {@code "debug"} (means {@link TemplateExceptionHandler#DEBUG_HANDLER}),
     *       {@code "html_debug"} (means {@link TemplateExceptionHandler#HTML_DEBUG_HANDLER}),
     *       {@code "ignore"} (means {@link TemplateExceptionHandler#IGNORE_HANDLER}),
     *       {@code "default"} (only allowed for {@link Configuration} instances) for the default.
     *       
     *   <li><p>{@code "arithmetic_engine"}:
     *       See {@link #setArithmeticEngine(ArithmeticEngine)}.  
     *       <br>String value: If the value contains dot, then it's interpreted as an <a href="#fm_obe">object builder
     *       expression</a>.
     *       If the value does not contain dot,
     *       then it must be one of these special values (case insensitive):
     *       {@code "bigdecimal"}, {@code "conservative"}.
     *       
     *   <li><p>{@code "object_wrapper"}:
     *       See {@link #setObjectWrapper(ObjectWrapper)}.
     *       <br>String value: If the value contains dot, then it's interpreted as an <a href="#fm_obe">object builder
     *       expression</a>, with the addition that {@link DefaultObjectWrapper}, {@link DefaultObjectWrapper} and
     *       {@link RestrictedObjectWrapper} can be referred without package name. For example, these strings are valid
     *       values: {@code "DefaultObjectWrapper(3.0.0)"},
     *       {@code "DefaultObjectWrapper(2.3.21, simpleMapWrapper=true)"}.
     *       <br>If the value does not contain dot, then it must be one of these special values (case insensitive):
     *       {@code "default"} means the default of {@link Configuration},
     *       {@code "restricted"} means the a {@link RestrictedObjectWrapper} instance.
     *
     *   <li><p>{@code "number_format"}: See {@link #setNumberFormat(String)}.
     *   
     *   <li><p>{@code "boolean_format"}: See {@link #setBooleanFormat(String)} .
     *   
     *   <li><p>{@code "date_format", "time_format", "datetime_format"}:
     *       See {@link #setDateFormat(String)}, {@link #setTimeFormat(String)}, {@link #setDateTimeFormat(String)}. 
     *        
     *   <li><p>{@code "time_zone"}:
     *       See {@link #setTimeZone(TimeZone)}.
     *       <br>String value: With the format as {@link TimeZone#getTimeZone} defines it. Also, since 2.3.21
     *       {@code "JVM default"} can be used that will be replaced with the actual JVM default time zone when
     *       {@link #setSetting(String, String)} is called.
     *       For example {@code "GMT-8:00"} or {@code "America/Los_Angeles"}
     *       <br>If you set this setting, consider setting {@code sql_date_and_time_time_zone}
     *       too (see below)! 
     *       
     *   <li><p>{@code sql_date_and_time_time_zone}:
     *       See {@link #setSQLDateAndTimeTimeZone(TimeZone)}.
     *       Since 2.3.21.
     *       <br>String value: With the format as {@link TimeZone#getTimeZone} defines it. Also, {@code "JVM default"}
     *       can be used that will be replaced with the actual JVM default time zone when
     *       {@link #setSetting(String, String)} is called. Also {@code "null"} can be used, which has the same effect
     *       as {@link #setSQLDateAndTimeTimeZone(TimeZone) setSQLDateAndTimeTimeZone(null)}.
     *       
     *   <li><p>{@code "output_encoding"}:
     *       See {@link #setOutputEncoding(Charset)}.
     *       
     *   <li><p>{@code "url_escaping_charset"}:
     *       See {@link #setURLEscapingCharset(Charset)}.
     *       
     *   <li><p>{@code "auto_flush"}:
     *       See {@link #setAutoFlush(boolean)}.
     *       Since 2.3.17.
     *       <br>String value: {@code "true"}, {@code "false"}, {@code "y"},  etc.
     *       
     *   <li><p>{@code "auto_import"}:
     *       See {@link Configuration#getAutoImports()}
     *       <br>String value is something like:
     *       <br>{@code /lib/form.ftl as f, /lib/widget as w, "/lib/odd name.ftl" as odd}
     *       
     *   <li><p>{@code "auto_include"}: Sets the list of auto-includes.
     *       See {@link Configuration#getAutoIncludes()}
     *       <br>String value is something like:
     *       <br>{@code /include/common.ftl, "/include/evil name.ftl"}
     *       
     *   <li><p>{@code "lazy_auto_imports"}:
     *       See {@link Configuration#getLazyAutoImports()}.
     *       <br>String value: {@code "true"}, {@code "false"} (also the equivalents: {@code "yes"}, {@code "no"},
     *       {@code "t"}, {@code "f"}, {@code "y"}, {@code "n"}), case insensitive. Also can be {@code "null"}.

     *   <li><p>{@code "lazy_imports"}:
     *       See {@link Configuration#getLazyImports()}.
     *       <br>String value: {@code "true"}, {@code "false"} (also the equivalents: {@code "yes"}, {@code "no"},
     *       {@code "t"}, {@code "f"}, {@code "y"}, {@code "n"}), case insensitive.
     *       
     *   <li><p>{@code "new_builtin_class_resolver"}:
     *       See {@link #setNewBuiltinClassResolver(TemplateClassResolver)}.
     *       Since 2.3.17.
     *       The value must be one of these (ignore the quotation marks):
     *       <ol>
     *         <li><p>{@code "unrestricted"}:
     *             Use {@link TemplateClassResolver#UNRESTRICTED_RESOLVER}
     *         <li><p>{@code "allows_nothing"}:
     *             Use {@link TemplateClassResolver#ALLOWS_NOTHING_RESOLVER}
     *         <li><p>Something that contains colon will use
     *             {@link OptInTemplateClassResolver} and is expected to
     *             store comma separated values (possibly quoted) segmented
     *             with {@code "allowed_classes:"} and/or
     *             {@code "trusted_templates:"}. Examples of valid values:
     *             
     *             <table style="width: auto; border-collapse: collapse" border="1"
     *                  summary="trusted_template value examples">
     *               <tr>
     *                 <th>Setting value
     *                 <th>Meaning
     *               <tr>
     *                 <td>
     *                   {@code allowed_classes: com.example.C1, com.example.C2,
     *                   trusted_templates: lib/*, safe.ftl}                 
     *                 <td>
     *                   Only allow instantiating the {@code com.example.C1} and
     *                   {@code com.example.C2} classes. But, allow templates
     *                   within the {@code lib/} directory (like
     *                   {@code lib/foo/bar.ftl}) and template {@code safe.ftl}
     *                   (that does not match {@code foo/safe.ftl}, only
     *                   exactly {@code safe.ftl}) to instantiate anything
     *                   that {@link TemplateClassResolver#UNRESTRICTED_RESOLVER} allows.
     *               <tr>
     *                 <td>
     *                   {@code allowed_classes: com.example.C1, com.example.C2}
     *                 <td>Only allow instantiating the {@code com.example.C1} and
     *                   {@code com.example.C2} classes. There are no
     *                   trusted templates.
     *               <tr>
     *                 <td>
                         {@code trusted_templates: lib/*, safe.ftl}                 
     *                 <td>
     *                   Do not allow instantiating any classes, except in
     *                   templates inside {@code lib/} or in template 
     *                   {@code safe.ftl}.
     *             </table>
     *             
     *             <p>For more details see {@link OptInTemplateClassResolver}.
     *             
     *         <li><p>Otherwise if the value contains dot, it's interpreted as an <a href="#fm_obe">object builder
     *             expression</a>.
     *       </ol>
     *       Note that the {@code safer} option was removed in FreeMarker 3.0.0, as it has become equivalent with
     *       {@code "unrestricted"}, as the classes it has blocked were removed from FreeMarker.
     *   <li><p>{@code "show_error_tips"}:
     *       See {@link #setShowErrorTips(boolean)}.
     *       Since 2.3.21.
     *       <br>String value: {@code "true"}, {@code "false"}, {@code "y"},  etc.
     *       
     *   <li><p>{@code api_builtin_enabled}:
     *       See {@link #setAPIBuiltinEnabled(boolean)}.
     *       Since 2.3.22.
     *       <br>String value: {@code "true"}, {@code "false"}, {@code "y"},  etc.
     *       
     * </ul>
     * 
     * <p>{@link Configuration} (a subclass of {@link MutableProcessingConfiguration}) also understands these:</p>
     * <ul>
     *   <li><p>{@code "auto_escaping"}:
     *       See {@link Configuration#getAutoEscapingPolicy()}
     *       <br>String value: {@code "enable_if_default"} or {@code "enableIfDefault"} for
     *       {@link AutoEscapingPolicy#ENABLE_IF_DEFAULT},
     *       {@code "enable_if_supported"} or {@code "enableIfSupported"} for
     *       {@link AutoEscapingPolicy#ENABLE_IF_SUPPORTED}
     *       {@code "disable"} for {@link AutoEscapingPolicy#DISABLE}.
     *       
     *   <li><p>{@code "sourceEncoding"}:
     *       See {@link Configuration#getSourceEncoding()}; since 2.3.26 also accepts value "JVM default"
     *       (not case sensitive) to set the Java environment default value.
     *       <br>As the default value is the system default, which can change
     *       from one server to another, <b>you should always set this!</b>
     *       
     *   <li><p>{@code "localized_lookup"}:
     *       See {@link Configuration#getLocalizedLookup()}.
     *       <br>String value: {@code "true"}, {@code "false"} (also the equivalents: {@code "yes"}, {@code "no"},
     *       {@code "t"}, {@code "f"}, {@code "y"}, {@code "n"}).
     *       ASTDirCase insensitive.
     *       
     *   <li><p>{@code "output_format"}:
     *       See {@link ParsingConfiguration#getOutputFormat()}.
     *       <br>String value: {@code "default"} (case insensitive) for the default, or an
     *       <a href="#fm_obe">object builder expression</a> that gives an {@link OutputFormat}, for example
     *       {@code HTMLOutputFormat} or {@code XMLOutputFormat}.
     *       
     *   <li><p>{@code "registered_custom_output_formats"}:
     *       See {@link Configuration#getRegisteredCustomOutputFormats()}.
     *       <br>String value: an <a href="#fm_obe">object builder expression</a> that gives a {@link List} of
     *       {@link OutputFormat}-s.
     *       Example: {@code [com.example.MyOutputFormat(), com.example.MyOtherOutputFormat()]}
     *       
     *   <li><p>{@code "whitespace_stripping"}:
     *       See {@link ParsingConfiguration#getWhitespaceStripping()}.
     *       <br>String value: {@code "true"}, {@code "false"}, {@code yes}, etc.
     *       
     *   <li><p>{@code "cache_storage"}:
     *       See {@link Configuration#getCacheStorage()}.
     *       <br>String value: If the value contains dot, then it's interpreted as an <a href="#fm_obe">object builder
     *       expression</a>.
     *       If the value does not contain dot,
     *       then a {@link org.apache.freemarker.core.templateresolver.impl.MruCacheStorage} will be used with the
     *       maximum strong and soft sizes specified with the setting value. Examples
     *       of valid setting values:
     *       
     *       <table style="width: auto; border-collapse: collapse" border="1" summary="cache_storage value examples">
     *         <tr><th>Setting value<th>max. strong size<th>max. soft size
     *         <tr><td>{@code "strong:50, soft:500"}<td>50<td>500
     *         <tr><td>{@code "strong:100, soft"}<td>100<td>{@code Integer.MAX_VALUE}
     *         <tr><td>{@code "strong:100"}<td>100<td>0
     *         <tr><td>{@code "soft:100"}<td>0<td>100
     *         <tr><td>{@code "strong"}<td>{@code Integer.MAX_VALUE}<td>0
     *         <tr><td>{@code "soft"}<td>0<td>{@code Integer.MAX_VALUE}
     *       </table>
     *       
     *       <p>The value is not case sensitive. The order of <tt>soft</tt> and <tt>strong</tt>
     *       entries is not significant.
     *       
     *   <li><p>{@code "template_update_delay"}:
     *       Template update delay in <b>seconds</b> (not in milliseconds) if no unit is specified; see
     *       {@link Configuration#getTemplateUpdateDelayMilliseconds()} for more.
     *       <br>String value: Valid positive integer, optionally followed by a time unit (recommended). The default
     *       unit is seconds. It's strongly recommended to specify the unit for clarity, like in "500 ms" or "30 s".
     *       Supported units are: "s" (seconds), "ms" (milliseconds), "m" (minutes), "h" (hours). The whitespace between
     *       the unit and the number is optional. Units are only supported since 2.3.23.
     *       
     *   <li><p>{@code "tag_syntax"}:
     *       See {@link ParsingConfiguration#getTagSyntax()}.
     *       <br>String value: Must be one of
     *       {@code "auto_detect"}, {@code "angle_bracket"}, and {@code "square_bracket"}. 
     *       
     *   <li><p>{@code "naming_convention"}:
     *       See {@link ParsingConfiguration#getNamingConvention()}.
     *       <br>String value: Must be one of
     *       {@code "auto_detect"}, {@code "legacy"}, and {@code "camel_case"}.
     *       
     *   <li><p>{@code "incompatible_improvements"}:
     *       See {@link Configuration#getIncompatibleImprovements()}.
     *       <br>String value: version number like {@code 2.3.20}.
     *       
     *   <li><p>{@code "recognize_standard_file_extensions"}:
     *       See {@link Configuration#getRecognizeStandardFileExtensions()}.
     *       <br>String value: {@code "default"} (case insensitive) for the default, or {@code "true"}, {@code "false"},
     *       {@code yes}, etc.
     *       
     *   <li><p>{@code "template_configurations"}:
     *       See: {@link Configuration#getTemplateConfigurations()}.
     *       <br>String value: Interpreted as an <a href="#fm_obe">object builder expression</a>,
     *       can be {@code null}.
     *       
     *   <li><p>{@code "template_loader"}:
     *       See: {@link Configuration#getTemplateLoader()}.
     *       <br>String value: {@code "default"} (case insensitive) for the default, or else interpreted as an
     *       <a href="#fm_obe">object builder expression</a>. {@code "null"} is also allowed.
     *       
     *   <li><p>{@code "template_lookup_strategy"}:
     *       See: {@link Configuration#getTemplateLookupStrategy()}.
     *       <br>String value: {@code "default"} (case insensitive) for the default, or else interpreted as an
     *       <a href="#fm_obe">object builder expression</a>.
     *       
     *   <li><p>{@code "template_name_format"}:
     *       See: {@link Configuration#getTemplateNameFormat()}.
     *       <br>String value: {@code "default"} (case insensitive) for the default, {@code "default_2_3_0"}
     *       for {@link DefaultTemplateNameFormatFM2#INSTANCE}, {@code "default_2_4_0"} for
     *       {@link DefaultTemplateNameFormat#INSTANCE}.
     * </ul>
     * 
     * <p><a name="fm_obe"></a>Regarding <em>object builder expressions</em> (used by the setting values where it was
     * indicated):
     * <ul>
     *   <li><p>Before FreeMarker 2.3.21 it had to be a fully qualified class name, and nothing else.</li>
     *   <li><p>Since 2.3.21, the generic syntax is:
     *       <tt><i>className</i>(<i>constrArg1</i>, <i>constrArg2</i>, ... <i>constrArgN</i>,
     *       <i>propName1</i>=<i>propValue1</i>, <i>propName2</i>=<i>propValue2</i>, ...
     *       <i>propNameN</i>=<i>propValueN</i>)</tt>,
     *       where
     *       <tt><i>className</i></tt> is the fully qualified class name of the instance to invoke (except if we have
     *       builder class or <tt>INSTANCE</tt> field around, but see that later),
     *       <tt><i>constrArg</i></tt>-s are the values of constructor arguments,
     *       and <tt><i>propName</i>=<i>propValue</i></tt>-s set JavaBean properties (like <tt>x=1</tt> means
     *       <tt>setX(1)</tt>) on the created instance. You can have any number of constructor arguments and property
     *       setters, including 0. Constructor arguments must precede any property setters.   
     *   </li>
     *   <li>
     *     Example: <tt>com.example.MyObjectWrapper(1, 2, exposeFields=true, cacheSize=5000)</tt> is nearly
     *     equivalent with this Java code:
     *     <tt>obj = new com.example.MyObjectWrapper(1, 2); obj.setExposeFields(true); obj.setCacheSize(5000);</tt>
     *   </li>
     *   <li>
     *      <p>If you have no constructor arguments and property setters, and the <tt><i>className</i></tt> class has
     *      a public static {@code INSTANCE} field, the value of that filed will be the value of the expression, and
     *      the constructor won't be called.
     *   </li>
     *   <li>
     *      <p>If there exists a class named <tt><i>className</i>Builder</tt>, then that class will be instantiated
     *      instead with the given constructor arguments, and the JavaBean properties of that builder instance will be
     *      set. After that, the public <tt>build()</tt> method of the instance will be called, whose return value
     *      will be the value of the whole expression. (The builder class and the <tt>build()</tt> method is simply
     *      found by name, there's no special interface to implement.)Note that if you have a builder class, you don't
     *      actually need a <tt><i>className</i></tt> class (since 2.3.24); after all,
     *      <tt><i>className</i>Builder.build()</tt> can return any kind of object. 
     *   </li>
     *   <li>
     *      <p>Currently, the values of arguments and properties can only be one of these:
     *      <ul>
     *        <li>A numerical literal, like {@code 123} or {@code -1.5}. The value will be automatically converted to
     *        the type of the target (just like in FTL). However, a target type is only available if the number will
     *        be a parameter to a method or constructor, not when it's a value (or key) in a {@code List} or
     *        {@code Map} literal. Thus in the last case the type of number will be like in Java language, like
     *        {@code 1} is an {@code int}, and {@code 1.0} is a {@code double}, and {@code 1.0f} is a {@code float},
     *        etc. In all cases, the standard Java type postfixes can be used ("f", "d", "l"), plus "bd" for
     *        {@code BigDecimal} and "bi" for {@code BigInteger}.</li>
     *        <li>A boolean literal: {@code true} or {@code false}
     *        <li>The null literal: {@code null}
     *        <li>A string literal with FTL syntax, except that  it can't contain <tt>${...}</tt>-s and
     *            <tt>#{...}</tt>-s. Examples: {@code "Line 1\nLine 2"} or {@code r"C:\temp"}.
     *        <li>A list literal (since 2.3.24) with FTL-like syntax, for example {@code [ 'foo', 2, true ]}.
     *            If the parameter is expected to be array, the list will be automatically converted to array.
     *            The list items can be any kind of expression, like even object builder expressions.
     *        <li>A map literal (since 2.3.24) with FTL-like syntax, for example <code>{ 'foo': 2, 'bar': true }</code>.
     *            The keys and values can be any kind of expression, like even object builder expressions.
     *            The resulting Java object will be a {@link Map} that keeps the item order ({@link LinkedHashMap} as
     *            of this writing).
     *        <li>A reference to a public static filed, like {@code Configuration.AUTO_DETECT} or
     *            {@code com.example.MyClass.MY_CONSTANT}.
     *        <li>An object builder expression. That is, object builder expressions can be nested into each other. 
     *      </ul>
     *   </li>
     *   <li>
     *     The same kind of expression as for parameters can also be used as top-level expressions (though it's
     *     rarely useful, apart from using {@code null}).
     *   </li>
     *   <li>
     *     <p>The top-level object builder expressions may omit {@code ()}.
     *   </li>
     *   <li>
     *     <p>The following classes can be referred to with simple (unqualified) name instead of fully qualified name:
     *     {@link DefaultObjectWrapper}, {@link DefaultObjectWrapper}, {@link RestrictedObjectWrapper}, {@link Locale},
     *     {@link TemplateConfiguration}, {@link PathGlobMatcher}, {@link FileNameGlobMatcher}, {@link PathRegexMatcher},
     *     {@link AndMatcher}, {@link OrMatcher}, {@link NotMatcher}, {@link ConditionalTemplateConfigurationFactory},
     *     {@link MergingTemplateConfigurationFactory}, {@link FirstMatchTemplateConfigurationFactory},
     *     {@link HTMLOutputFormat}, {@link XMLOutputFormat}, {@link RTFOutputFormat}, {@link PlainTextOutputFormat},
     *     {@link UndefinedOutputFormat}, {@link Configuration}, {@link TemplateLanguage}, {@link NamingConvention},
     *     {@link TagSyntax}.
     *   </li>
     *   <li>
     *     <p>{@link TimeZone} objects can be created like {@code TimeZone("UTC")}, despite that there's no a such
     *     constructor.
     *   </li>
     *   <li>
     *     <p>{@link Charset} objects can be created like {@code Charset("ISO-8859-5")}, despite that there's no a such
     *     constructor.
     *   </li>
     *   <li>
     *     <p>The classes and methods that the expression meant to access must be all public.
     *   </li>
     * </ul>
     * 
     * @param name the name of the setting.
     * @param value the string that describes the new value of the setting.
     * 
     * @throws UnknownConfigurationSettingException if the name is wrong.
     * @throws ConfigurationSettingValueException if the new value of the setting can't be set for any other reasons.
     */
    public void setSetting(String name, String value) throws ConfigurationException {
        boolean unknown = false;
        try {
            if (LOCALE_KEY.equals(name)) {
                if (JVM_DEFAULT_VALUE.equalsIgnoreCase(value)) {
                    setLocale(Locale.getDefault());
                } else {
                    setLocale(_StringUtil.deduceLocale(value));
                }
            } else if (NUMBER_FORMAT_KEY_SNAKE_CASE.equals(name) || NUMBER_FORMAT_KEY_CAMEL_CASE.equals(name)) {
                setNumberFormat(value);
            } else if (CUSTOM_NUMBER_FORMATS_KEY_SNAKE_CASE.equals(name)
                    || CUSTOM_NUMBER_FORMATS_KEY_CAMEL_CASE.equals(name)) {
                Map map = (Map) _ObjectBuilderSettingEvaluator.eval(
                                value, Map.class, false, _SettingEvaluationEnvironment.getCurrent());
                checkSettingValueItemsType("Map keys", String.class, map.keySet());
                checkSettingValueItemsType("Map values", TemplateNumberFormatFactory.class, map.values());
                setCustomNumberFormats(map);
            } else if (TIME_FORMAT_KEY_SNAKE_CASE.equals(name) || TIME_FORMAT_KEY_CAMEL_CASE.equals(name)) {
                setTimeFormat(value);
            } else if (DATE_FORMAT_KEY_SNAKE_CASE.equals(name) || DATE_FORMAT_KEY_CAMEL_CASE.equals(name)) {
                setDateFormat(value);
            } else if (DATETIME_FORMAT_KEY_SNAKE_CASE.equals(name) || DATETIME_FORMAT_KEY_CAMEL_CASE.equals(name)) {
                setDateTimeFormat(value);
            } else if (CUSTOM_DATE_FORMATS_KEY_SNAKE_CASE.equals(name)
                    || CUSTOM_DATE_FORMATS_KEY_CAMEL_CASE.equals(name)) {
                Map map = (Map) _ObjectBuilderSettingEvaluator.eval(
                                value, Map.class, false, _SettingEvaluationEnvironment.getCurrent());
                checkSettingValueItemsType("Map keys", String.class, map.keySet());
                checkSettingValueItemsType("Map values", TemplateDateFormatFactory.class, map.values());
                setCustomDateFormats(map);
            } else if (TIME_ZONE_KEY_SNAKE_CASE.equals(name) || TIME_ZONE_KEY_CAMEL_CASE.equals(name)) {
                setTimeZone(parseTimeZoneSettingValue(value));
            } else if (SQL_DATE_AND_TIME_TIME_ZONE_KEY_SNAKE_CASE.equals(name)
                    || SQL_DATE_AND_TIME_TIME_ZONE_KEY_CAMEL_CASE.equals(name)) {
                setSQLDateAndTimeTimeZone(value.equals("null") ? null : parseTimeZoneSettingValue(value));
            } else if (TEMPLATE_EXCEPTION_HANDLER_KEY_SNAKE_CASE.equals(name)
                    || TEMPLATE_EXCEPTION_HANDLER_KEY_CAMEL_CASE.equals(name)) {
                if (value.indexOf('.') == -1) {
                    if ("debug".equalsIgnoreCase(value)) {
                        setTemplateExceptionHandler(
                                TemplateExceptionHandler.DEBUG_HANDLER);
                    } else if ("html_debug".equalsIgnoreCase(value) || "htmlDebug".equals(value)) {
                        setTemplateExceptionHandler(
                                TemplateExceptionHandler.HTML_DEBUG_HANDLER);
                    } else if ("ignore".equalsIgnoreCase(value)) {
                        setTemplateExceptionHandler(
                                TemplateExceptionHandler.IGNORE_HANDLER);
                    } else if ("rethrow".equalsIgnoreCase(value)) {
                        setTemplateExceptionHandler(
                                TemplateExceptionHandler.RETHROW_HANDLER);
                    } else if (DEFAULT_VALUE.equalsIgnoreCase(value)
                            && this instanceof Configuration.ExtendableBuilder) {
                        unsetTemplateExceptionHandler();
                    } else {
                        throw new ConfigurationSettingValueException(
                                name, value,
                                "No such predefined template exception handler name");
                    }
                } else {
                    setTemplateExceptionHandler((TemplateExceptionHandler) _ObjectBuilderSettingEvaluator.eval(
                            value, TemplateExceptionHandler.class, false, _SettingEvaluationEnvironment.getCurrent()));
                }
            } else if (ARITHMETIC_ENGINE_KEY_SNAKE_CASE.equals(name) || ARITHMETIC_ENGINE_KEY_CAMEL_CASE.equals(name)) {
                if (value.indexOf('.') == -1) { 
                    if ("bigdecimal".equalsIgnoreCase(value)) {
                        setArithmeticEngine(BigDecimalArithmeticEngine.INSTANCE);
                    } else if ("conservative".equalsIgnoreCase(value)) {
                        setArithmeticEngine(ConservativeArithmeticEngine.INSTANCE);
                    } else {
                        throw new ConfigurationSettingValueException(
                                name, value, "No such predefined arithmetical engine name");
                    }
                } else {
                    setArithmeticEngine((ArithmeticEngine) _ObjectBuilderSettingEvaluator.eval(
                            value, ArithmeticEngine.class, false, _SettingEvaluationEnvironment.getCurrent()));
                }
            } else if (OBJECT_WRAPPER_KEY_SNAKE_CASE.equals(name) || OBJECT_WRAPPER_KEY_CAMEL_CASE.equals(name)) {
                if (DEFAULT_VALUE.equalsIgnoreCase(value)) {
                    if (this instanceof Configuration.ExtendableBuilder) {
                        this.unsetObjectWrapper();
                    } else {
                        // FM3 TODO should depend on IcI
                        setObjectWrapper(new DefaultObjectWrapper.Builder(Configuration.VERSION_3_0_0).build());
                    }
                } else if ("restricted".equalsIgnoreCase(value)) {
                    // FM3 TODO should depend on IcI
                    setObjectWrapper(new RestrictedObjectWrapper.Builder(Configuration.VERSION_3_0_0).build());
                } else {
                    setObjectWrapper((ObjectWrapper) _ObjectBuilderSettingEvaluator.eval(
                                    value, ObjectWrapper.class, false, _SettingEvaluationEnvironment.getCurrent()));
                }
            } else if (BOOLEAN_FORMAT_KEY_SNAKE_CASE.equals(name) || BOOLEAN_FORMAT_KEY_CAMEL_CASE.equals(name)) {
                setBooleanFormat(value);
            } else if (OUTPUT_ENCODING_KEY_SNAKE_CASE.equals(name) || OUTPUT_ENCODING_KEY_CAMEL_CASE.equals(name)) {
                setOutputEncoding(Charset.forName(value));
            } else if (URL_ESCAPING_CHARSET_KEY_SNAKE_CASE.equals(name)
                    || URL_ESCAPING_CHARSET_KEY_CAMEL_CASE.equals(name)) {
                setURLEscapingCharset(Charset.forName(value));
            } else if (AUTO_FLUSH_KEY_SNAKE_CASE.equals(name) || AUTO_FLUSH_KEY_CAMEL_CASE.equals(name)) {
                setAutoFlush(_StringUtil.getYesNo(value));
            } else if (SHOW_ERROR_TIPS_KEY_SNAKE_CASE.equals(name) || SHOW_ERROR_TIPS_KEY_CAMEL_CASE.equals(name)) {
                setShowErrorTips(_StringUtil.getYesNo(value));
            } else if (API_BUILTIN_ENABLED_KEY_SNAKE_CASE.equals(name)
                    || API_BUILTIN_ENABLED_KEY_CAMEL_CASE.equals(name)) {
                setAPIBuiltinEnabled(_StringUtil.getYesNo(value));
            } else if (NEW_BUILTIN_CLASS_RESOLVER_KEY_SNAKE_CASE.equals(name)
                    || NEW_BUILTIN_CLASS_RESOLVER_KEY_CAMEL_CASE.equals(name)) {
                if ("unrestricted".equals(value)) {
                    setNewBuiltinClassResolver(TemplateClassResolver.UNRESTRICTED_RESOLVER);
                } else if ("allows_nothing".equals(value) || "allowsNothing".equals(value)) {
                    setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
                } else if (value.indexOf(":") != -1) {
                    List<_KeyValuePair<String, List<String>>> segments = parseAsSegmentedList(value);
                    Set allowedClasses = null;
                    List<String> trustedTemplates = null;
                    for (_KeyValuePair<String, List<String>> segment : segments) {
                        String segmentKey = segment.getKey();
                        List<String> segmentValue = segment.getValue();
                        if (segmentKey.equals(ALLOWED_CLASSES)) {
                            allowedClasses = new HashSet(segmentValue);
                        } else if (segmentKey.equals(TRUSTED_TEMPLATES)) {
                            trustedTemplates = segmentValue;
                        } else {
                            throw new ConfigurationSettingValueException(name, value,
                                    "Unrecognized list segment key: " + _StringUtil.jQuote(segmentKey) +
                                            ". Supported keys are: \"" + ALLOWED_CLASSES + "\", \"" +
                                            TRUSTED_TEMPLATES + "\"");
                        }
                    }
                    setNewBuiltinClassResolver(
                            new OptInTemplateClassResolver(allowedClasses, trustedTemplates));
                } else if (value.indexOf('.') != -1) {
                    setNewBuiltinClassResolver((TemplateClassResolver) _ObjectBuilderSettingEvaluator.eval(
                                    value, TemplateClassResolver.class, false,
                                    _SettingEvaluationEnvironment.getCurrent()));
                } else {
                    throw new ConfigurationSettingValueException(
                            name, value,
                            "Not predefined class resolved name, nor follows class resolver definition syntax, nor "
                            + "looks like class name");
                }
            } else if (LOG_TEMPLATE_EXCEPTIONS_KEY_SNAKE_CASE.equals(name)
                    || LOG_TEMPLATE_EXCEPTIONS_KEY_CAMEL_CASE.equals(name)) {
                setLogTemplateExceptions(_StringUtil.getYesNo(value));
            } else if (LAZY_AUTO_IMPORTS_KEY_SNAKE_CASE.equals(name) || LAZY_AUTO_IMPORTS_KEY_CAMEL_CASE.equals(name)) {
                setLazyAutoImports(value.equals(NULL_VALUE) ? null : Boolean.valueOf(_StringUtil.getYesNo(value)));
            } else if (LAZY_IMPORTS_KEY_SNAKE_CASE.equals(name) || LAZY_IMPORTS_KEY_CAMEL_CASE.equals(name)) {
                setLazyImports(_StringUtil.getYesNo(value));
            } else if (AUTO_INCLUDE_KEY_SNAKE_CASE.equals(name)
                    || AUTO_INCLUDE_KEY_CAMEL_CASE.equals(name)) {
                setAutoIncludes(parseAsList(value));
            } else if (AUTO_IMPORT_KEY_SNAKE_CASE.equals(name) || AUTO_IMPORT_KEY_CAMEL_CASE.equals(name)) {
                setAutoImports(parseAsImportList(value));
            } else {
                unknown = true;
            }
        } catch (ConfigurationSettingValueException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationSettingValueException(name, value, e);
        }
        if (unknown) {
            throw unknownSettingException(name);
        }
    }

    /**
     * Fluent API equivalent of {@link #setSetting(String, String)}.
     */
    public SelfT setting(String name, String value) throws ConfigurationException {
        setSetting(name, value);
        return self();
    }

    /**
     * @throws IllegalArgumentException
     *             if the type of the some of the values isn't as expected
     */
    private void checkSettingValueItemsType(String somethingsSentenceStart, Class<?> expectedClass,
                                                  Collection<?> values) {
        if (values == null) return;
        for (Object value : values) {
            if (!expectedClass.isInstance(value)) {
                throw new IllegalArgumentException(somethingsSentenceStart + " must be instances of "
                        + _ClassUtil.getShortClassName(expectedClass) + ", but one of them was a(n) "
                        + _ClassUtil.getShortClassNameOfObject(value) + ".");
            }
        }
    }

    /**
     * Returns the valid setting names that aren't {@link Configuration}-only.
     *
     * @param camelCase
     *            If we want the setting names with camel case naming convention, or with snake case (legacy) naming
     *            convention.
     * 
     * @see Configuration.ExtendableBuilder#getSettingNames(boolean)
     * 
     * @since 2.3.24
     */
    public static Set<String> getSettingNames(boolean camelCase) {
        return new _SortedArraySet<>(camelCase ? SETTING_NAMES_CAMEL_CASE : SETTING_NAMES_SNAKE_CASE);
    }

    private TimeZone parseTimeZoneSettingValue(String value) {
        TimeZone tz;
        if (JVM_DEFAULT_VALUE.equalsIgnoreCase(value)) {
            tz = TimeZone.getDefault();
        } else {
            tz = TimeZone.getTimeZone(value);
        }
        return tz;
    }
    
    /**
     * Creates the exception that should be thrown when a setting name isn't recognized.
     */
    protected final UnknownConfigurationSettingException unknownSettingException(String name) {
        Version removalVersion = getRemovalVersionForUnknownSetting(name);
         return removalVersion != null
                ? new UnknownConfigurationSettingException(name, removalVersion)
                : new UnknownConfigurationSettingException(name, getCorrectedNameForUnknownSetting(name));
    }

    /**
     * If a setting name is unknown because it was removed over time (not just renamed), then returns the version where
     * it was removed, otherwise returns {@code null}.
     */
    protected Version getRemovalVersionForUnknownSetting(String name) {
        if (name.equals("classic_compatible") || name.equals("classicCompatible")) {
            return Configuration.VERSION_3_0_0;
        }
        return null;
    }
    
    /**
     * @param name The wrong name
     * @return The corrected name, or {@code null} if there's no known correction
     * @since 2.3.21
     */
    protected String getCorrectedNameForUnknownSetting(String name) {
        return null;
    }
    
    /**
     * Set the settings stored in a <code>Properties</code> object.
     * 
     * @throws ConfigurationException if the <code>Properties</code> object contains
     *     invalid keys, or invalid setting values, or any other error occurs
     *     while changing the settings.
     */    
    public void setSettings(Properties props) throws ConfigurationException {
        final _SettingEvaluationEnvironment prevEnv = _SettingEvaluationEnvironment.startScope();
        try {
            for (String key : props.stringPropertyNames()) {
                setSetting(key, props.getProperty(key).trim());
            }
        } finally {
            _SettingEvaluationEnvironment.endScope(prevEnv);
        }
    }

    /**
     * Fluent API equivalent of {@link #setSettings(Properties)}.
     */
    public SelfT settings(Properties props) {
        setSettings(props);
        return self();
    }

    @Override
    public Map<Object, Object> getCustomAttributes() {
        return isCustomAttributesSet() ? customAttributes : getDefaultCustomAttributes();
    }

    protected abstract Map<Object,Object> getDefaultCustomAttributes();

    /**
     * Setter pair of {@link #getCustomAttributes()}
     *
     * @param customAttributes Not {@code null}. The {@link Map} is copied to prevent aliasing problems.
     */
    public void setCustomAttributes(Map<Object, Object> customAttributes) {
        setCustomAttributesWithoutCopying(new LinkedHashMap<>(customAttributes));
    }

    /**
     * Fluent API equivalent of {@link #setCustomAttributes(Map)}
     */
    public SelfT customAttributes(Map<Object, Object> customAttributes) {
        setCustomAttributes(customAttributes);
        return self();
    }

    /**
     * Used internally instead of {@link #setCustomAttributes(Map)} to speed up use cases where we know that there
     * won't be aliasing problems.
     */
    void setCustomAttributesWithoutCopying(Map<Object, Object> customAttributes) {
        _NullArgumentException.check("customAttributes", customAttributes);
        this.customAttributes = customAttributes;
    }

    @Override
    public boolean isCustomAttributesSet() {
        return customAttributes != null;
    }

    boolean isCustomAttributeSet(Object key) {
         return isCustomAttributesSet() && customAttributes.containsKey(key);
    }

    /**
     * Sets a {@linkplain #getCustomAttributes() custom attribute} for this configurable.
     *
     * @param name
     *         the name of the custom attribute
     * @param value
     *         the value of the custom attribute. You can set the value to {@code null}, however note that there is a
     *         semantic difference between an attribute set to {@code null} and an attribute that is not present (see
     *         {@link #removeCustomAttribute(Object)}).
     */
    public void setCustomAttribute(Object name, Object value) {
        if (customAttributes == null) {
            customAttributes = new LinkedHashMap<>();
        }
        customAttributes.put(name, value);
    }

    /**
     * Fluent API equivalent of {@link #setCustomAttribute(Object, Object)}
     */
    public SelfT customAttribute(Object name, Object value) {
        setCustomAttribute(name, value);
        return self();
    }

    /**
     * Returns an array with names of all custom attributes defined directly on this {@link ProcessingConfiguration}.
     * (That is, it doesn't contain the names of custom attributes inherited from other {@link
     * ProcessingConfiguration}-s.) The returned array is never {@code null}, but can be zero-length.
     */
    // TODO env only?
    // TODO should return List<String>?
    public String[] getCustomAttributeNames() {
        if (customAttributes == null) {
            return _CollectionUtil.EMPTY_STRING_ARRAY;
        }
        Collection names = new LinkedList(customAttributes.keySet());
        for (Iterator iter = names.iterator(); iter.hasNext(); ) {
            if (!(iter.next() instanceof String)) {
                iter.remove();
            }
        }
        return (String[]) names.toArray(new String[names.size()]);
    }
    
    /**
     * Removes a named custom attribute for this configurable. Note that this
     * is different than setting the custom attribute value to null. If you
     * set the value to null, {@link #getCustomAttribute(Object)} will return
     * null, while if you remove the attribute, it will return the value of
     * the attribute in the parent configurable (if there is a parent 
     * configurable, that is). 
     *
     * @param name the name of the custom attribute
     */
    // TODO doesn't work properly, remove?
    public void removeCustomAttribute(Object name) {
        if (customAttributes == null) {
            return;
        }
        customAttributes.remove(name);
    }

    @Override
    public Object getCustomAttribute(Object key) {
        Object value;
        if (customAttributes != null) {
            value = customAttributes.get(key);
            if (value == null && customAttributes.containsKey(key)) {
                return null;
            }
        } else {
            value = null;
        }
        return value != null ? value : getDefaultCustomAttribute(key);
    }

    protected abstract Object getDefaultCustomAttribute(Object name);

    protected final List<String> parseAsList(String text) throws GenericParseException {
        return new SettingStringParser(text).parseAsList();
    }

    protected final List<_KeyValuePair<String, List<String>>> parseAsSegmentedList(String text)
    throws GenericParseException {
        return new SettingStringParser(text).parseAsSegmentedList();
    }
    
    private final HashMap parseAsImportList(String text) throws GenericParseException {
        return new SettingStringParser(text).parseAsImportList();
    }

    @SuppressWarnings("unchecked")
    protected SelfT self() {
        return (SelfT) this;
    }
    
    /**
     * Helper class for parsing setting values given with string.
     */
    private static class SettingStringParser {
        private String text;
        private int p;
        private int ln;

        private SettingStringParser(String text) {
            this.text = text;
            p = 0;
            ln = text.length();
        }

        List<_KeyValuePair<String, List<String>>> parseAsSegmentedList() throws GenericParseException {
            List<_KeyValuePair<String, List<String>>> segments = new ArrayList();
            List<String> currentSegment = null;
            
            char c;
            while (true) {
                c = skipWS();
                if (c == ' ') break;
                String item = fetchStringValue();
                c = skipWS();
                
                if (c == ':') {
                    currentSegment = new ArrayList();
                    segments.add(new _KeyValuePair<>(item, currentSegment));
                } else {
                    if (currentSegment == null) {
                        throw new GenericParseException(
                                "The very first list item must be followed by \":\" so " +
                                "it will be the key for the following sub-list.");
                    }
                    currentSegment.add(item);
                }
                
                if (c == ' ') break;
                if (c != ',' && c != ':') throw new GenericParseException(
                        "Expected \",\" or \":\" or the end of text but " +
                        "found \"" + c + "\"");
                p++;
            }
            return segments;
        }

        ArrayList parseAsList() throws GenericParseException {
            char c;
            ArrayList seq = new ArrayList();
            while (true) {
                c = skipWS();
                if (c == ' ') break;
                seq.add(fetchStringValue());
                c = skipWS();
                if (c == ' ') break;
                if (c != ',') throw new GenericParseException(
                        "Expected \",\" or the end of text but " +
                        "found \"" + c + "\"");
                p++;
            }
            return seq;
        }

        HashMap parseAsImportList() throws GenericParseException {
            char c;
            HashMap map = new HashMap();
            while (true) {
                c = skipWS();
                if (c == ' ') break;
                String lib = fetchStringValue();

                c = skipWS();
                if (c == ' ') throw new GenericParseException(
                        "Unexpected end of text: expected \"as\"");
                String s = fetchKeyword();
                if (!s.equalsIgnoreCase("as")) throw new GenericParseException(
                        "Expected \"as\", but found " + _StringUtil.jQuote(s));

                c = skipWS();
                if (c == ' ') throw new GenericParseException(
                        "Unexpected end of text: expected gate hash name");
                String ns = fetchStringValue();
                
                map.put(ns, lib);

                c = skipWS();
                if (c == ' ') break;
                if (c != ',') throw new GenericParseException(
                        "Expected \",\" or the end of text but "
                        + "found \"" + c + "\"");
                p++;
            }
            return map;
        }

        String fetchStringValue() throws GenericParseException {
            String w = fetchWord();
            if (w.startsWith("'") || w.startsWith("\"")) {
                w = w.substring(1, w.length() - 1);
            }
            return FTLUtil.unescapeStringLiteralPart(w);
        }

        String fetchKeyword() throws GenericParseException {
            String w = fetchWord();
            if (w.startsWith("'") || w.startsWith("\"")) {
                throw new GenericParseException(
                    "Keyword expected, but a string value found: " + w);
            }
            return w;
        }

        char skipWS() {
            char c;
            while (p < ln) {
                c = text.charAt(p);
                if (!Character.isWhitespace(c)) return c;
                p++;
            }
            return ' ';
        }

        private String fetchWord() throws GenericParseException {
            if (p == ln) throw new GenericParseException(
                    "Unexpeced end of text");

            char c = text.charAt(p);
            int b = p;
            if (c == '\'' || c == '"') {
                boolean escaped = false;
                char q = c;
                p++;
                while (p < ln) {
                    c = text.charAt(p);
                    if (!escaped) {
                        if (c == '\\') {
                            escaped = true;
                        } else if (c == q) {
                            break;
                        }
                    } else {
                        escaped = false;
                    }
                    p++;
                }
                if (p == ln) {
                    throw new GenericParseException("Missing " + q);
                }
                p++;
                return text.substring(b, p);
            } else {
                do {
                    c = text.charAt(p);
                    if (!(Character.isLetterOrDigit(c)
                            || c == '/' || c == '\\' || c == '_'
                            || c == '.' || c == '-' || c == '!'
                            || c == '*' || c == '?')) break;
                    p++;
                } while (p < ln);
                if (b == p) {
                    throw new GenericParseException("Unexpected character: " + c);
                } else {
                    return text.substring(b, p);
                }
            }
        }
    }
    
}
