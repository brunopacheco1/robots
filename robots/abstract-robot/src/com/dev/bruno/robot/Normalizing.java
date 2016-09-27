package com.dev.bruno.robot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dev.bruno.dto.DocumentType;

@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface Normalizing {

	public DocumentType documentType();
}