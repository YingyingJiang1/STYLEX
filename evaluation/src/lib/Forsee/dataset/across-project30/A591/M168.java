    private StringArg readStringArg() throws IOException, FilterException {
	int c = reader.read();
	int last = 0;
	if (c != '"') {
	    throw new FilterException("Check the parser, trying to read a string that doesn't begin with quotes");
	}
	StringBuffer buffer = new StringBuffer();
	while (reader.ready()) {
	    last = c;
	    c = reader.read();
	    if (c == -1) {
		break;
	    }
	    
	    if (c == '"' && last != '\\') {
		return new StringArg(buffer.toString());
	    } else {
		buffer.append((char)c);
	    }
	}
	throw new FilterException("Unterminated string");
    }
