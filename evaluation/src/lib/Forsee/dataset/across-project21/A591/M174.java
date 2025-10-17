	private LogEntry readNextEntry() {
	    LogEntry e = null;
	    try {
		long crcValue;
		byte[] bytes;
		try {
		    crcValue = logStream.readLong("crcvalue");
		    
		    bytes = logStream.readBuffer("txnEntry");
		} catch (EOFException ex) {
		    return null;
		}
		
		if (bytes.length == 0) {
		    return null;
		}
		Checksum crc = new Adler32();
		crc.update(bytes, 0, bytes.length);
		if (crcValue != crc.getValue()) {
		    throw new IOException("CRC doesn't match " + crcValue +
					  " vs " + crc.getValue());
		}
    
		TxnLogEntry logEntry = SerializeUtils.deserializeTxn(bytes);
		TxnHeader hdr = logEntry.getHeader();
		Record r = logEntry.getTxn();

		switch (hdr.getType()) {
		case OpCode.createSession: {
		    e = new TransactionEntry(hdr.getTime(), hdr.getClientId(), hdr.getCxid(), hdr.getZxid(), "createSession");
		}
		    break;
		case OpCode.closeSession: {
		    e = new TransactionEntry(hdr.getTime(), hdr.getClientId(), hdr.getCxid(), hdr.getZxid(), "closeSession");
		}
		    break;
		case OpCode.create:
		    if (r != null) {
			CreateTxn create = (CreateTxn)r;
			String path = create.getPath();
			e = new TransactionEntry(hdr.getTime(), hdr.getClientId(), hdr.getCxid(), hdr.getZxid(), "create", path);
		    }
		    break;
		case OpCode.setData:
		    if (r != null) {
			SetDataTxn set = (SetDataTxn)r;
			String path = set.getPath();
			e = new TransactionEntry(hdr.getTime(), hdr.getClientId(), hdr.getCxid(), hdr.getZxid(), "setData", path);
		    }
		    break;
		case OpCode.setACL:
		    if (r != null) {
			SetACLTxn setacl = (SetACLTxn)r;
			String path = setacl.getPath();
		    e = new TransactionEntry(hdr.getTime(), hdr.getClientId(), hdr.getCxid(), hdr.getZxid(), "setACL", path);
		    }
		    break;
		case OpCode.error:
		    if (r != null)  {
			ErrorTxn error = (ErrorTxn)r;
			
			e = new TransactionEntry(hdr.getTime(), hdr.getClientId(), hdr.getCxid(), hdr.getZxid(), "error", "Error: " + error.getErr());
		    }
		    break;
		default:
		    LOG.info("Unknown op: " + hdr.getType());
		    break;
		}
		
		if (logStream.readByte("EOR") != 'B') {
		    throw new EOFException("Last transaction was partial.");
		}
	    } catch (Exception ex) {
		LOG.error("Error reading transaction from (" + src.file + ") :" + e);
		return null;
	    }
	    return e;
	}
