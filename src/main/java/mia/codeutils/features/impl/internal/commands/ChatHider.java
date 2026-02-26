package mia.codeutils.features.impl.internal.commands;

import joptsimple.util.RegexMatcher;

import java.util.function.Consumer;
import java.util.regex.Matcher;

public record ChatHider(RegexMatcher regex, Consumer<Matcher> callback) { }
