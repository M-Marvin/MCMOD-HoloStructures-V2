package de.m_marvin.blueprints.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.m_marvin.blueprints.api.IStructAccessor;

/**
 * An blueprint parser is basically an implementation of an specific file format, and can only load that specific format it is designed for
 * 
 * @author Marvin Koehler
 */
public interface IBlueprintParser {
	
	/**
	 * Loads a file, checks if it is of the correct format, and prepares it to be parsed.
	 * The data of the file is stored internally, and the file is closed and no longer needed for parsing after this method returns.
	 * @param istream The iostream to read the file contents from
	 * @return true if the file is of the correct format and could be loaded
	 * @throws IOException
	 */
	public boolean load(InputStream istream) throws IOException;
	
	/**
	 * Writes the currently stored data to an file.
	 * @param ostream The iostream to write the file contents
	 * @return true if the data could successfully written
	 * @throws IOException
	 */
	public boolean write(OutputStream ostream) throws IOException;
	
	/**
	 * Tries to parse the stored data loaded from an file and paste it to an {@link IStructAccessor} implementation.
	 * <b>NOTE</b>: That an file was identified to be of the correct format and being successfully loaded, does not guarantee the parsing to be successful.<br>
	 * <b>NOTE</b>: If some of the data could not be parsed, more details are stored within the target using {@link IStructAccessor#logParseWarn()}
	 * @param target The target to paste the data in, normally an {@link Blueprint}
	 * @return true if the data could partially been parsed
	 */
	public boolean parse(IStructAccessor target);
	
	/**
	 * Tries to read the date from the {@link IStructAccessor} and store it internally to be written to an file later.
	 * <b>NOTE</b>: If some of the data could not be stored, more details are stored within the target using {@link IStructAccessor#logParseWarn()}
	 * @param source The target to read the data from, normally an {@link Blueprint}
	 * @return true if the data could partially been stored.
	 */
	public boolean build(IStructAccessor source);
	
	/**
	 * Clears the internally stored data.
	 */
	public void reset();
	
	/** Helper method used to store error messages within the targets when
	 * @param accessor The target to store the message
	 * @param message The message format string
	 * @param arguments The message arguments
	 */
	public static void logWarn(IStructAccessor accessor, String message, Object... arguments) {
		accessor.logParseWarn(String.format(message, arguments));
	}
	
}
