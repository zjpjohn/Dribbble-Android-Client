/*
 * Copyright 2015 Google Inc.
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

package com.lucas.freeshots.model;

import java.io.Serializable;

/**
 * 此类属性的名称要与服务器返回的字段名称一致，
 * 所以命名不符合Java编程规范。
 */
public class AccessToken implements Serializable {

    public String access_token;
    public String token_type;
    public String scope;

    @Override
    public String toString() {
        return String.format("access_token=%s, token_type=%s, scope=%s",
                                access_token, token_type, scope);
    }
}
