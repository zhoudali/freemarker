/*
 * Copyright 2014 Attila Szegedi, Daniel Dekany, Jonathan Revusky
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package freemarker.core;

import java.util.Locale;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

public class LocaleSensitiveTemplateNumberFormatFactory extends TemplateNumberFormatFactory {

    public static final LocaleSensitiveTemplateNumberFormatFactory INSTANCE = new LocaleSensitiveTemplateNumberFormatFactory();
    
    private LocaleSensitiveTemplateNumberFormatFactory() {
        // Defined to decrease visibility
    }
    
    @Override
    public LocaleSensitiveLocalTemplateNumberFormatFactory createLocalFactory(Environment env, Locale locale) {
        return new LocaleSensitiveLocalTemplateNumberFormatFactory(locale);
    }
    
    private static class LocaleSensitiveLocalTemplateNumberFormatFactory extends LocalTemplateNumberFormatFactory {

        LocaleSensitiveLocalTemplateNumberFormatFactory(Locale locale) {
            super(null, locale);
        }

        @Override
        public TemplateNumberFormat get(String params) throws InvalidFormatParametersException {
            TemplateNumberFormatUtil.checkHasNoParameters(params);
            return new LocaleSensitiveTemplateNumberFormat(getLocale());
        }

        @Override
        protected void onLocaleChanged() {
            // No op
        }
        
        private static class LocaleSensitiveTemplateNumberFormat extends TemplateNumberFormat {

            private final Locale locale;
            
            private LocaleSensitiveTemplateNumberFormat(Locale locale) {
                this.locale = locale;
            }
            
            @Override
            public String format(TemplateNumberModel numberModel)
                    throws UnformattableNumberException, TemplateModelException {
                Number n = numberModel.getAsNumber();
                try {
                    return n + "_" + locale;
                } catch (ArithmeticException e) {
                    throw new UnformattableNumberException(n + " doesn't fit into an int");
                }
            }

            @Override
            public <MO extends TemplateMarkupOutputModel> MO format(TemplateNumberModel dateModel,
                    MarkupOutputFormat<MO> outputFormat) throws UnformattableNumberException, TemplateModelException {
                return null;
            }

            @Override
            public boolean isLocaleBound() {
                return true;
            }

            @Override
            public String getDescription() {
                return "test locale sensitive";
            }
            
        }
        
    }

}