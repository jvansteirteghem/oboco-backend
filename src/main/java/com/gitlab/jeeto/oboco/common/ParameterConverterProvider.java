package com.gitlab.jeeto.oboco.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

@Provider
public class ParameterConverterProvider implements ParamConverterProvider {

    @SuppressWarnings("unchecked")
	@Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if(rawType.equals(Long.class)) {
            return (ParamConverter<T>) new LongParameterConverter();
        } else if(rawType.equals(Integer.class)) {
        	return (ParamConverter<T>) new IntegerParameterConverter();
        }
        return null;
    }
    
    public static class IntegerParameterConverter implements ParamConverter<Integer> {

        @Override
        public Integer fromString(String value) {
            if (value == null)
                return null;
            return Integer.valueOf(value);
        }

        @Override
        public String toString(Integer value) {
            if (value == null)
                return null;
            return value.toString();
        }

    }
    
    public static class LongParameterConverter implements ParamConverter<Long> {

        @Override
        public Long fromString(String value) {
            if (value == null)
                return null;
            return Long.valueOf(value);
        }

        @Override
        public String toString(Long value) {
            if (value == null)
                return null;
            return value.toString();
        }

    }
}
