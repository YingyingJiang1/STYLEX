    @Override
    public List<T> apply(List<T> a, List<T> b) {
        int n = a.size() + b.size();
        if (n == 0) {
            return new ArrayList<>();
        }
        List<T> both = new ArrayList<>(n);

        Iterator<T> at = a.iterator();
        Iterator<T> bt = b.iterator();

        T s1 = at.hasNext() ? at.next() : null;
        T s2 = bt.hasNext() ? bt.next() : null;

        while (s1 != null && s2 != null) {
            if (comparator.compare(s1, s2) < 0) { // s1 comes before s2
                both.add(s1);
                s1 = at.hasNext() ? at.next() : null;
            } else {
                both.add(s2);
                s2 = bt.hasNext() ? bt.next() : null;
            }
        }

        if (s1 != null) {
            both.add(s1);
            while (at.hasNext()) {
                both.add(at.next());
            }
        } else {
            both.add(s2);
            while (bt.hasNext()) {
                both.add(bt.next());
            }
        }

        return both;
    }
