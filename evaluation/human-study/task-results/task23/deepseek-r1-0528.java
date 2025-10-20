
private void init() throws IOException {
    File f = new File(file);
    try (RandomAccessFileReader in = new RandomAccessFileReader(f)) {
        long lastFp = in.getPosition();
        String line = in.readLine();
        Matcher m = null;

        if ((line != null) && (m = timep.matcher(line)).lookingAt()) {
            starttime = timestampFromText(dateformat, m.group(1));
        } else {
            throw new IOException("Invalid log format. First line doesn't start with time");
        }

        String lastentry = line;
        try {
            while (line != null) {
                m = timep.matcher(line);
                if (m.lookingAt()) {
                    if (size % skipN == 0) {
                        long time = timestampFromText(dateformat, m.group(1));
                        skiplist.addMark(time, lastFp, size);
                    }
                    size++;
                    lastentry = line;
                } 
                if (serverid == 0 && (m = idp.matcher(line)).find()) {
                    serverid = Integer.valueOf(m.group(1));
                }

                lastFp = in.getPosition();
                line = in.readLine();
            }
        } catch (EOFException eof) {
            // Ignored
        }

        m = timep.matcher(lastentry);
        if (m.lookingAt()) {
            endtime = timestampFromText(dateformat, m.group(1));
        } else {
            throw new IOException("Invalid log format. Last line doesn't start with time");
        }
    }
}
