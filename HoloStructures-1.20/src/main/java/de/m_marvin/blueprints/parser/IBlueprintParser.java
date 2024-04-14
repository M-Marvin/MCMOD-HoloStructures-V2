package de.m_marvin.blueprints.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.m_marvin.blueprints.api.IStructAccessor;

public interface IBlueprintParser {
	
	public boolean load(InputStream istream) throws IOException;
	public boolean write(OutputStream ostream) throws IOException;
	
	public boolean parse(IStructAccessor target);
	public boolean build(IStructAccessor source);
	public void reset();
	
	public static void logWarn(IStructAccessor accessor, String message, Object... arguments) {
		accessor.logParseWarn(String.format(message, arguments));
	}
	
}
