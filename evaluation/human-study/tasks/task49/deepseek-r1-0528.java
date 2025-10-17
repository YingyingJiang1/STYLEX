
@Override
public void close() throws IOException {
    boolean triedToClose = false;
    boolean success = false;
    try {
        flush();
        ((FileOutputStream) out).getFD().sync();
        triedToClose = true;
        super.close();
        success = true;
    } finally {
        if (success) {
            boolean renamed = tmpFile.renameTo(origFile);
            if (!renamed) {
                if (!origFile.delete() || !tmpFile.renameTo(origFile)) {
                    throw new IOException("Could not rename temporary file " + 
                            tmpFile + " to " + origFile);
                }
            }
        } else {
            if (!triedToClose) {
                IOUtils.closeStream(out);
            }
            if (!tmpFile.delete()) {
                LOG.warn("Unable to delete tmp file {}", tmpFile);
            }
        }
    }
}
