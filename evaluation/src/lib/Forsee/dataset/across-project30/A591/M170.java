    public FilterOp parse() throws IOException, FilterException {
	int c = reader.read();
	if (c != '(') {
	    throw new FilterException("Invalid format");
	}

	String opstr = readUntilSpace();
	FilterOp op = FilterOp.newOp(opstr);

	while (reader.ready()) {
	    c = reader.read();
	    if (c == -1) {
		break;
	    }
	    if (c == '(') {
		reader.unread(c);
		op.addSubOp(parse());
	    } else if (c == ')') {
		return op;
	    } else if (c == '"') {
		reader.unread(c);
		op.addArg(readStringArg());
	    } else if (Character.isDigit(c) || c == '-' || c == '+') {
		reader.unread(c);
		op.addArg(readNumberArg());
	    } else if (Character.isJavaIdentifierStart(c)) {
		reader.unread(c);
		op.addArg(readSymbolArg());
	    }
	}
	throw new FilterException("Incomplete filter");
    }
