/**
 *  Copyright 2017 FinTx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package feign.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.lang.reflect.Type;

/**
 * @author bluecreator(qiang.x.wang@gmail.com)
 *
 */
public class FastjsonEncoder implements Encoder {
    private SerializeConfig config = null;

    public FastjsonEncoder() {
        this.config=SerializeConfig.getGlobalInstance();
    }

    public FastjsonEncoder(SerializeConfig config) {
        this.config = config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see feign.codec.Encoder#encode(java.lang.Object, java.lang.reflect.Type, feign.RequestTemplate)
     */
    @Override
    public void encode(Object arg0, Type arg1, RequestTemplate arg2) throws EncodeException {
        try {
            JavaType javaType = mapper.getTypeFactory().constructType(bodyType);
            template.body(mapper.writerFor(javaType).writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new EncodeException(e.getMessage(), e);
        }
    }

}
