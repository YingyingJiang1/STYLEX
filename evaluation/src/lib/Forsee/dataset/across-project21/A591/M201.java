    synchronized public int read(byte[] buf, int off, int len) throws IOException {
	if (LOG.isTraceEnabled()) {
	    LOG.trace("read(buf, off=" + off + ", len=" + len);
	}

	int read = 0;
	while (len > 0) {
	    if (buffersize == 0) {
		fill();
		if (buffersize == 0) {
		    break;
		}
	    }

	    int tocopy = Math.min(len, buffersize);
	    if (LOG.isTraceEnabled()) {
		LOG.trace("tocopy=" + tocopy);
	    }

	    System.arraycopy(buffer, bufferoffset, buf, off, tocopy);
	    buffersize -= tocopy;
	    bufferoffset += tocopy;

	    len -= tocopy;
	    read += tocopy;
	    off += tocopy;
	}
	if (LOG.isTraceEnabled()) {
	    LOG.trace("read=" + read);
	}

	return read;
    }
