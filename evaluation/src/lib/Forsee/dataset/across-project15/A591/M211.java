    public static void main(String[] args) throws IOException {
	RandomAccessFileReader f = new RandomAccessFileReader(new File(args[0]));
	
	long pos0 = f.getPosition();
	for (int i = 0; i < 5; i++) {
	    System.out.println(f.readLine());
	}
	System.out.println("=============");
	long pos1 = f.getPosition();
	System.out.println("pos: " + pos1);
	for (int i = 0; i < 5; i++) {
	    System.out.println(f.readLine());
	}
	System.out.println("=============");
	f.seek(pos1);
	for (int i = 0; i < 5; i++) {
	    System.out.println(f.readLine());
	}
	System.out.println("=============");
	f.seek(pos0);
	for (int i = 0; i < 5; i++) {
	    System.out.println(f.readLine());
	}
	long pos2 = f.getPosition();
	System.out.println("=============");
	System.out.println(f.readLine());
	f.seek(pos2);
	System.out.println(f.readLine());
	f.close();
    }
