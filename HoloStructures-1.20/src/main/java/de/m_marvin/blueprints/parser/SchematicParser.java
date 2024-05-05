package de.m_marvin.blueprints.parser;

import java.io.InputStream;
import java.io.OutputStream;

import de.m_marvin.blueprints.api.IBlueprintAcessor;

/**
 * An implementation of {@link IBlueprintParser} for the pre 1.13 .schematic format.
 * 
 * @author Marvin Koehler
 */
public class SchematicParser implements IBlueprintParser {

	// TODO .schematic parser implementation
	
	@Override
	public boolean load(InputStream istream) {
		return false;
	}

	@Override
	public boolean write(OutputStream ostream) {
		return false;
	}

	@Override
	public boolean parse(IBlueprintAcessor target) {
		return false;
	}

	@Override
	public void reset() {
	}
	
	@Override
	public boolean build(IBlueprintAcessor source) {
		return false;
	}

}
