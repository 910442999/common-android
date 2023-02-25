/*
 * Copyright (C) 2015 Square, Inc.
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
package com.yuanquan.common.api.gson;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.yuanquan.common.api.response.BaseResult;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String jsonString = value.string();
        try {
            //处理接口返回数据异常情况
            if (jsonString.isEmpty()) {
                BaseResult result = new BaseResult();
                result.setCode(-99999999);
                result.setMessage("服务器返回数据是空的，手动设置，防止框架异常问题");
                return adapter.fromJson(gson.toJson(result));
            } else if (jsonString.contains("code") || jsonString.contains("data")) {
                BaseResult httpResponse = gson.fromJson(jsonString, BaseResult.class);
//                if (httpResponse.getCode() != 0) {
//                    httpResponse.setData(null);
//                    return adapter.fromJson(gson.toJson(httpResponse));
//                } else
                    if (httpResponse.getData() == null || TextUtils.isEmpty(httpResponse.getData().toString()) || httpResponse.getData().toString().equals("{}")) {
                    httpResponse.setData(null);
                    return adapter.fromJson(gson.toJson(httpResponse));
                }
            }
            return adapter.fromJson(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            return adapter.fromJson(jsonString);
        } finally {
            value.close();
        }
    }
}
