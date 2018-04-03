/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.freemarker.core;

/**
 * Used as the value of the {@link ParsingConfiguration#getInterpolationSyntax()}  interpolationSyntax} setting.
 */
public enum InterpolationSyntax {
    
    /** <code>${expression}</code> */
    DOLLAR,
    
    /**
     * <code>[=expression]</code> instead of <code>${expression}</code>.
     * It does <em>not</em> change {@code <#if x>} to {@code [#if x]}; that's square bracket <em>tag</em> syntax
     * ({@link TagSyntax#SQUARE_BRACKET}).
     */
    SQUARE_BRACKET;
}