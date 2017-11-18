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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.IOUtils;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * @author bluecreator(qiang.x.wang@gmail.com)
 *
 */
public class FastjsonDecoder implements Decoder {
    private ParserConfig config = null;

    public FastjsonDecoder() {
        this(null);
    }

    public FastjsonDecoder(ParserConfig config) {
        if (null != config) {
            this.config = config;
        } else {
            this.config = ParserConfig.getGlobalInstance();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see feign.codec.Decoder#decode(feign.Response, java.lang.reflect.Type)
     */
    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (response.status() == 404)
            return Util.emptyValueOf(type);
        if (response.body() == null)
            return null;
        InputStream in = response.body().asInputStream();
        if (!in.markSupported()) {
            in = new BufferedInputStream(in, 2);
        }

        // Read the first byte to see if we have any data
        in.mark(1);
        if (in.read() == -1) {
            return null; // Eagerly returning null avoids "No content to map due to end-of-input"
        }
        in.reset();

        return parseObject(in, null, type);

    }

    @SuppressWarnings("unchecked")
    public <T> T parseObject(InputStream is, //
            Charset charset, //
            Type type, //
            Feature...features) throws IOException {
        if (charset == null) {
            charset = IOUtils.UTF8;
        }

        byte[] bytes = allocateBytes(1024 * 64);
        int offset = 0;
        for (;;) {
            int readCount = is.read(bytes, offset, bytes.length - offset);
            if (readCount == -1) {
                break;
            }
            offset += readCount;
            if (offset == bytes.length) {
                byte[] newBytes = new byte[bytes.length * 3 / 2];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
            }
        }

        return (T) parseObject(bytes, 0, offset, charset, type, features);
    }

    /**
     * @since 1.2.11
     */
    @SuppressWarnings("unchecked")
    public <T> T parseObject(byte[] bytes, int offset, int len, Charset charset, Type clazz, Feature...features) {
        if (charset == null) {
            charset = IOUtils.UTF8;
        }

        String strVal;
        if (charset == IOUtils.UTF8) {
            char[] chars = allocateChars(bytes.length);
            int chars_len = IOUtils.decodeUTF8(bytes, offset, len, chars);
            if (chars_len < 0) {
                return null;
            }
            strVal = new String(chars, 0, chars_len);
        } else {
            if (len < 0) {
                return null;
            }
            strVal = new String(bytes, offset, len, charset);
        }
        return JSON.parseObject(strVal, clazz, config, features);
    }

    private final static ThreadLocal<char[]> charsLocal = new ThreadLocal<char[]>();

    private static char[] allocateChars(int length) {
        char[] chars = charsLocal.get();

        if (chars == null) {
            if (length <= 1024 * 64) {
                chars = new char[1024 * 64];
                charsLocal.set(chars);
            } else {
                chars = new char[length];
            }
        } else if (chars.length < length) {
            chars = new char[length];
        }

        return chars;
    }

    private final static ThreadLocal<byte[]> bytesLocal = new ThreadLocal<byte[]>();

    private static byte[] allocateBytes(int length) {
        byte[] chars = bytesLocal.get();

        if (chars == null) {
            if (length <= 1024 * 64) {
                chars = new byte[1024 * 64];
                bytesLocal.set(chars);
            } else {
                chars = new byte[length];
            }
        } else if (chars.length < length) {
            chars = new byte[length];
        }

        return chars;
    }

}
