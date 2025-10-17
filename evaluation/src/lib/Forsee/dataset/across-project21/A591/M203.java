    synchronized public String readLine() throws IOException {
	StringBuffer s = null;
	
	// go through buffer until i find a \n, if i reach end of buffer first, put whats in buffer into string buffer,
	// repeat
	buffering:
	for (;;) {
	    if (buffersize == 0) {
		fill();
		if (buffersize == 0) {
		    break;
		}
	    }

	    for (int i = 0; i < buffersize; i++) {
		if (buffer[bufferoffset + i] == '\n') { 
		    if (i > 0) { // if \n is first char in buffer, leave the string buffer empty
			if (s == null) { s = new StringBuffer(); }
			s.append(new String(buffer, bufferoffset, i, "UTF-8"));
		    }
		    bufferoffset += i+1;
		    buffersize -= i+1; 
		    break buffering;
		}
	    }

	    // We didn't find \n, read the whole buffer into string buffer
	    if (s == null) { s = new StringBuffer(); }
	    s.append(new String(buffer, bufferoffset, buffersize, "UTF-8"));
	    buffersize = 0; 
	}

	if (s == null) {
	    return null;
	} else {
	    return s.toString();
	}	    
    }
