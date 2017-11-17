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

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.parser.deserializer.ExtraTypeProvider;
import com.alibaba.fastjson.parser.deserializer.FieldTypeResolver;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.util.IOUtils;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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
        if(null!=config) {
            this.config=config;
        }else {
            this.config=ParserConfig.getGlobalInstance();
        }
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see feign.codec.Decoder#decode(feign.Response, java.lang.reflect.Type)
     */
    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (response.status() == 404) return Util.emptyValueOf(type);
        if (response.body() == null) return null;
        Reader reader = response.body().asReader();
        if (!reader.markSupported()) {
          reader = new BufferedReader(reader, 1);
        }
        try {
          // Read the first byte to see if we have any data
          reader.mark(1);
          if (reader.read() == -1) {
            return null; // Eagerly returning null avoids "No content to map due to end-of-input"
          }
          reader.reset();
          return mapper.readValue(reader, mapper.constructType(type));
        } catch (RuntimeJsonMappingException e) {
          if (e.getCause() != null && e.getCause() instanceof IOException) {
            throw IOException.class.cast(e.getCause());
          }
          throw e;
        }
      }
    @SuppressWarnings("unchecked")
    public static <T> T parseObject(String input, Type clazz, ParserConfig config, ParseProcess processor,
                                          int featureValues, Feature... features) {
        if (input == null) {
            return null;
        }

        if (features != null) {
            for (Feature feature : features) {
                featureValues |= feature.mask;
            }
        }

        DefaultJSONParser parser = new DefaultJSONParser(input, config, featureValues);

        if (processor != null) {
            if (processor instanceof ExtraTypeProvider) {
                parser.getExtraTypeProviders().add((ExtraTypeProvider) processor);
            }

            if (processor instanceof ExtraProcessor) {
                parser.getExtraProcessors().add((ExtraProcessor) processor);
            }

            if (processor instanceof FieldTypeResolver) {
                parser.setFieldTypeResolver((FieldTypeResolver) processor);
            }
        }

        T value = (T) parser.parseObject(clazz, null);

        parser.handleResovleTask(value);

        parser.close();

        return (T) value;
    }
    /**
     * @since 1.2.11
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseObject(InputStream is, //
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
    public static <T> T parseObject(byte[] bytes, int offset, int len, Charset charset, Type clazz, Feature...features) {
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
        return (T) parseObject(strVal, clazz, config,features);
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
