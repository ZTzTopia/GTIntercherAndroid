package com.anzu.sdk;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Cube<E> implements Iterable<E> {
    Iterable<E> data;

    public interface Calculator<I> {
        double calculate(I i, int i2);
    }

    public interface Classify<I, O> {
        O groupBy(I i, int i2);
    }

    public interface Comparator<I> {
        int compareTo(I i, I i2);
    }

    public interface Convertible<I, O> {
        O transform(I i, int i2);
    }

    public interface Equality<I> {
        boolean equals(I i, I i2);
    }

    public interface Predicate<I> {
        boolean predicate(I i, int i2);
    }

    private static class NotImplementedException extends RuntimeException {
        private NotImplementedException() {
            /* ~ */
        }
    }

    private static class Content<T> {
        T value;

        private Content() {
        }

        public T value() {
            return this.value;
        }

        public void value(T t) {
            this.value = t;
        }
    }

    public static class Selection<T> implements Predicate<T>, Calculator<T>, Equality<T>, Comparator<T> {
        @Override
        public boolean predicate(T t, int i) {
            throw new NotImplementedException();
        }

        @Override
        public double calculate(T t, int i) {
            throw new NotImplementedException();
        }

        @Override
        public boolean equals(T t, T t2) {
            throw new NotImplementedException();
        }

        @Override
        public int compareTo(T t, T t2) {
            throw new NotImplementedException();
        }
    }

    public static class Conversion<I, O> implements Convertible<I, O>, Classify<I, O> {
        @Override
        public O transform(I i, int i2) {
            throw new NotImplementedException();
        }

        @Override
        public O groupBy(I i, int i2) {
            throw new NotImplementedException();
        }
    }

    public static Cube<Integer> forCount(int i) {
        Integer[] numArr = new Integer[i];
        for (int i2 = 0; i2 < i; i2++) {
            numArr[i2] = i2;
        }
        return from(numArr);
    }

    public static Cube<Boolean> from(boolean... zArr) {
        Boolean[] boolArr = new Boolean[zArr.length];
        for (int i = 0; i < zArr.length; i++) {
            boolArr[i] = zArr[i];
        }
        return from(boolArr);
    }

    public static Cube<Byte> from(byte... bArr) {
        Byte[] bArr2 = new Byte[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            bArr2[i] = bArr[i];
        }
        return from(bArr2);
    }

    public static Cube<Short> from(short... sArr) {
        Short[] shArr = new Short[sArr.length];
        for (int i = 0; i < sArr.length; i++) {
            shArr[i] = sArr[i];
        }
        return from(shArr);
    }

    public static Cube<Integer> from(int... iArr) {
        Integer[] numArr = new Integer[iArr.length];
        for (int i = 0; i < iArr.length; i++) {
            numArr[i] = iArr[i];
        }
        return from(numArr);
    }

    public static Cube<Long> from(long... jArr) {
        Long[] lArr = new Long[jArr.length];
        for (int i = 0; i < jArr.length; i++) {
            lArr[i] = jArr[i];
        }
        return from(lArr);
    }

    public static Cube<Float> from(float... fArr) {
        Float[] fArr2 = new Float[fArr.length];
        for (int i = 0; i < fArr.length; i++) {
            fArr2[i] = fArr[i];
        }
        return from(fArr2);
    }

    public static Cube<Double> from(double... dArr) {
        Double[] dArr2 = new Double[dArr.length];
        for (int i = 0; i < dArr.length; i++) {
            dArr2[i] = dArr[i];
        }
        return from(dArr2);
    }

    public static Cube<Character> from(char... cArr) {
        Character[] chArr = new Character[cArr.length];
        for (int i = 0; i < cArr.length; i++) {
            chArr[i] = cArr[i];
        }
        return from(chArr);
    }

    @SafeVarargs
    public static <T> Cube<T> from(T... tArr) {
        return new Cube<>(tArr);
    }

    public static <T> Cube<T> from(Iterable<T> iterable) {
        return new Cube<>(iterable);
    }

    public static <T> Cube<T> from(Enumeration<T> enumeration) {
        ArrayList newArrayList = newArrayList(new Object[0]);
        while (enumeration.hasMoreElements()) {
            newArrayList.add(enumeration.nextElement());
        }
        return from(newArrayList);
    }

    public static int size(Iterable iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException();
        } else if (iterable instanceof Collection) {
            return ((Collection) iterable).size();
        } else {
            Iterator it = iterable.iterator();
            int i = 0;
            while (it.hasNext()) {
                it.next();
                i++;
            }
            return i;
        }
    }

    public static int size(Enumeration enumeration) {
        int i = 0;
        while (enumeration.hasMoreElements()) {
            enumeration.nextElement();
            i++;
        }
        return i;
    }

    public static <T> Cube<T> emptyCube() {
        return new Cube<>((T[]) new Object[0]);
    }

    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    @SafeVarargs
    public static <T> List<T> asList(T... tArr) {
        return new ArrayList(Arrays.asList(tArr));
    }

    public static <T> List<T> asList(Iterable<T> iterable) {
        if (iterable instanceof Cube) {
            return ((Cube) iterable).toList();
        }
        ArrayList newArrayList = newArrayList(new Object[0]);
        if (iterable != null && iterable.iterator().hasNext()) {
            for (T t : iterable) {
                newArrayList.add(t);
            }
        }
        return newArrayList;
    }

    public static <T> List<T> asList(Enumeration<T> enumeration) {
        ArrayList newArrayList = newArrayList(new Object[0]);
        if (enumeration == null) {
            return newArrayList;
        }
        while (enumeration.hasMoreElements()) {
            newArrayList.add(enumeration.nextElement());
        }
        return newArrayList;
    }

    @SafeVarargs
    public static <T> ArrayList<T> newArrayList(T... tArr) {
        return new ArrayList<>(Arrays.asList(tArr));
    }

    public static <T> ArrayList<T> newArrayList(Iterable<T> iterable) {
        ArrayList<T> arrayList = new ArrayList<>();
        if (iterable == null) {
            return arrayList;
        }
        for (T t : iterable) {
            arrayList.add(t);
        }
        return arrayList;
    }

    @SafeVarargs
    public static <T> LinkedList<T> newLinkedList(T... tArr) {
        return new LinkedList<>(Arrays.asList(tArr));
    }

    public static <T> LinkedList<T> newLinkedList(Iterable<T> iterable) {
        LinkedList<T> linkedList = new LinkedList<>();
        if (iterable == null) {
            return linkedList;
        }
        for (T t : iterable) {
            linkedList.add(t);
        }
        return linkedList;
    }

    @SafeVarargs
    public static <T> CopyOnWriteArrayList<T> newCopyOnWriteArrayList(T... tArr) {
        return new CopyOnWriteArrayList<>(Arrays.asList(tArr));
    }

    public static <T> CopyOnWriteArrayList<T> newCopyOnWriteArrayList(Iterable<T> iterable) {
        CopyOnWriteArrayList<T> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        if (iterable == null) {
            return copyOnWriteArrayList;
        }
        for (T t : iterable) {
            copyOnWriteArrayList.add(t);
        }
        return copyOnWriteArrayList;
    }

    public static <T> Set<T> emptySet() {
        return Collections.emptySet();
    }

    @SafeVarargs
    public static <T> HashSet<T> newHashSet(T... tArr) {
        return new HashSet<>(Arrays.asList(tArr));
    }

    public static <T> HashSet<T> newHashSet(Iterable<T> iterable) {
        HashSet<T> hashSet = new HashSet<>();
        if (iterable == null) {
            return hashSet;
        }
        for (T t : iterable) {
            hashSet.add(t);
        }
        return hashSet;
    }

    public static <T> Set<T> newConcurrentHashSet() {
        return Collections.newSetFromMap(new ConcurrentHashMap());
    }

    public static <K, V> Map<K, V> emptyMap() {
        return new Map<K, V>() {
            /* class com.anzu.sdk.Cube.AnonymousClass1 */

            public boolean containsKey(Object obj) {
                return false;
            }

            public boolean containsValue(Object obj) {
                return false;
            }

            public boolean isEmpty() {
                return true;
            }

            public int size() {
                return 0;
            }

            @Override // java.util.Map
            public V get(Object obj) {
                throw new UnsupportedOperationException();
            }

            @Override // java.util.Map
            public V put(K k, V v) {
                throw new UnsupportedOperationException();
            }

            @Override // java.util.Map
            public V remove(Object obj) {
                throw new UnsupportedOperationException();
            }

            @Override // java.util.Map
            public void putAll(Map<? extends K, ? extends V> map) {
                throw new UnsupportedOperationException();
            }

            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override // java.util.Map
            public Set<K> keySet() {
                return Collections.emptySet();
            }

            @Override // java.util.Map
            public Collection<V> values() {
                return Collections.emptyList();
            }

            @Override // java.util.Map
            public Set<Map.Entry<K, V>> entrySet() {
                return Collections.emptySet();
            }
        };
    }

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    public static <T> LinkedBlockingQueue<T> newLinkedBlockingQueue() {
        return new LinkedBlockingQueue<>();
    }

    @SafeVarargs
    private Cube(E... eArr) {
        if (eArr == null || eArr.length == 0) {
            this.data = (Iterable<E>) newArrayList(new Object[0]);
        } else {
            this.data = asList(eArr);
        }
    }

    private Cube(Iterable<E> iterable) {
        if (iterable == null) {
            this.data = (Iterable<E>) newArrayList(new Object[0]);
        } else if (iterable instanceof Cube) {
            this.data = ((Cube) iterable).data;
        } else {
            this.data = iterable;
        }
    }

    @Override // java.lang.Iterable
    public final Iterator<E> iterator() {
        return this.data.iterator();
    }

    public final List<E> toList() {
        Iterable<E> iterable = this.data;
        if (iterable instanceof List) {
            return (List) iterable;
        }
        ArrayList newArrayList = newArrayList(new Object[0]);
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            newArrayList.add(it.next());
        }
        return newArrayList;
    }

    public final Set<E> toSet() {
        Iterable<E> iterable = this.data;
        if (iterable instanceof Set) {
            return (Set) iterable;
        }
        HashSet newHashSet = newHashSet(new Object[0]);
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            newHashSet.add(it.next());
        }
        return newHashSet;
    }

    public final <O> O[] toArray(Class<O> cls) {
        List<E> list = toList();
        O[] oArr = (O[]) ((Object[]) Array.newInstance((Class<?>) cls, list.size()));
        for (int i = 0; i < list.size(); i++) {
            oArr[i] = cls.cast(list.get(i));
        }
        return oArr;
    }

    public final boolean any() {
        return iterator().hasNext();
    }

    public final boolean any(Predicate<E> predicate) {
        if (predicate == null) {
            return iterator().hasNext();
        }
        Iterator<E> it = iterator();
        int i = 0;
        while (it.hasNext()) {
            if (predicate.predicate(it.next(), i)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public final boolean has(E e) {
        if (e == null) {
            return false;
        }
        Iterable<E> iterable = this.data;
        if (iterable instanceof Collection) {
            return ((Collection) iterable).contains(e);
        }
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            E next = it.next();
            if (next != null && next.equals(e)) {
                return true;
            }
        }
        return false;
    }

    public final int count() {
        return size(this);
    }

    public final int count(Predicate<E> predicate) {
        if (predicate != null) {
            Iterator<E> it = iterator();
            int i = 0;
            int i2 = 0;
            while (it.hasNext()) {
                if (predicate.predicate(it.next(), i2)) {
                    i++;
                }
                i2++;
            }
            return i;
        }
        throw new IllegalArgumentException();
    }

    public final double sum() {
        double d = 0.0d;
        if (!any()) {
            return 0.0d;
        }
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            E next = it.next();
            if (next instanceof Number) {
                d += ((Number) next).doubleValue();
            } else {
                throw new IllegalArgumentException(String.format("%s is not Number type", next));
            }
        }
        return d;
    }

    public final double sum(Calculator<E> calculator) {
        if (calculator != null) {
            double d = 0.0d;
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                d += calculator.calculate(it.next(), i);
                i++;
            }
            return d;
        }
        throw new IllegalArgumentException();
    }

    public final double max() {
        if (!any()) {
            return Double.NaN;
        }
        double d = Double.MIN_VALUE;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            E next = it.next();
            if (next instanceof Number) {
                d = Math.max(d, ((Number) next).doubleValue());
            } else {
                throw new IllegalArgumentException(String.format("%s is not Number type", next));
            }
        }
        return d;
    }

    public final double max(Calculator<E> calculator) {
        if (calculator != null) {
            double d = Double.MIN_VALUE;
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                d = Math.max(d, calculator.calculate(it.next(), i));
                i++;
            }
            return d;
        }
        throw new IllegalArgumentException();
    }

    public final double min() {
        if (!any()) {
            return Double.NaN;
        }
        double d = Double.MAX_VALUE;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            E next = it.next();
            if (next instanceof Number) {
                d = Math.min(d, ((Number) next).doubleValue());
            } else {
                throw new IllegalArgumentException(String.format("%s is not Number type", next));
            }
        }
        return d;
    }

    public final double min(Calculator<E> calculator) {
        if (calculator != null) {
            double d = Double.MAX_VALUE;
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                d = Math.min(d, calculator.calculate(it.next(), i));
                i++;
            }
            return d;
        }
        throw new IllegalArgumentException();
    }

    public final E maxOne(Calculator<E> calculator) {
        if (calculator != null) {
            E e = null;
            double d = Double.MIN_VALUE;
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                E next = it.next();
                double calculate = calculator.calculate(next, i);
                if (d != calculate) {
                    d = Math.max(d, calculate);
                    if (d == calculate) {
                        e = next;
                    }
                }
                i++;
            }
            return e;
        }
        throw new IllegalArgumentException();
    }

    public final E minOne(Calculator<E> calculator) {
        if (calculator != null) {
            E e = null;
            double d = Double.MAX_VALUE;
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                E next = it.next();
                double calculate = calculator.calculate(next, i);
                if (d != calculate) {
                    d = Math.min(d, calculate);
                    if (d == calculate) {
                        e = next;
                    }
                }
                i++;
            }
            return e;
        }
        throw new IllegalArgumentException();
    }

    public final E first() {
        if (any()) {
            return iterator().next();
        }
        return null;
    }

    public final E first(Predicate<E> predicate) {
        if (predicate != null) {
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                E next = it.next();
                if (predicate.predicate(next, i)) {
                    return next;
                }
                i++;
            }
            return null;
        }
        throw new IllegalArgumentException();
    }

    public final E last() {
        List<E> list = toList();
        return list.get(list.size() - 1);
    }

    public final E last(Predicate<E> predicate) {
        if (predicate != null) {
            E e = null;
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                E next = it.next();
                if (predicate.predicate(next, i)) {
                    e = next;
                }
                i++;
            }
            return e;
        }
        throw new IllegalArgumentException();
    }

    public final E random() {
        List<E> list = toList();
        if (any()) {
            return list.get(new SecureRandom().nextInt(list.size()));
        }
        return null;
    }

    @SafeVarargs
    public final Cube<E> concat(E... eArr) {
        List<E> list = toList();
        for (E e : eArr) {
            list.add(e);
        }
        return from(list);
    }

    public final Cube<E> concat(Iterable<E> iterable) {
        List<E> list = toList();
        for (E e : iterable) {
            list.add(e);
        }
        return from(list);
    }

    public final Cube<E> concat(Enumeration<E> enumeration) {
        List<E> list = toList();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return from(list);
    }

    public final Cube<E> distinct() {
        return from(toSet());
    }

    public final Cube<E> distinct(Equality<E> equality) {
        if (equality != null) {
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                E next = it.next();
                Iterator<E> it2 = newArrayList.iterator();
                while (true) {
                    if (it2.hasNext()) {
                        if (equality.equals(next, it2.next())) {
                            break;
                        }
                    } else {
                        newArrayList.add(next);
                        break;
                    }
                }
            }
            return from(newArrayList);
        }
        throw new IllegalArgumentException();
    }

    public final Cube<E> skip(int i) {
        if (i >= 0) {
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            int i2 = 0;
            while (it.hasNext()) {
                if (i2 < i) {
                    it.next();
                } else {
                    newArrayList.add(it.next());
                }
                i2++;
            }
            if (i2 >= i) {
                return from(newArrayList);
            }
            throw new IndexOutOfBoundsException(String.format("size: %d < %d", Integer.valueOf(i2), Integer.valueOf(i)));
        }
        throw new IllegalArgumentException();
    }

    public final Cube<E> skipUntil(Predicate<E> predicate) {
        if (predicate != null) {
            boolean z = false;
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                E next = it.next();
                if (z || (z = predicate.predicate(next, i))) {
                    newArrayList.add(next);
                }
                i++;
            }
            return from(newArrayList);
        }
        throw new IllegalArgumentException();
    }

    public final Cube<E> take(int i) {
        if (i >= 0) {
            int i2 = 0;
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            while (it.hasNext() && i2 < i) {
                newArrayList.add(it.next());
                i2++;
            }
            return from(newArrayList);
        }
        throw new IllegalArgumentException();
    }

    public final Cube<E> takeUntil(Predicate<E> predicate) {
        if (predicate != null) {
            int i = 0;
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                E next = it.next();
                if (predicate.predicate(next, i)) {
                    break;
                }
                newArrayList.add(next);
                i++;
            }
            return from(newArrayList);
        }
        throw new IllegalArgumentException();
    }

    public final Cube<E> reverse() {
        List<E> list = toList();
        Collections.reverse(list);
        return from(list);
    }

    public final Cube<E> notNull() {
        ArrayList newArrayList = newArrayList(new Object[0]);
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            E next = it.next();
            if (next != null) {
                newArrayList.add(next);
            }
        }
        return from(newArrayList);
    }

    public final Cube<E> where(Predicate<E> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException();
        } else if (!iterator().hasNext()) {
            return emptyCube();
        } else {
            int i = 0;
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                E next = it.next();
                if (predicate.predicate(next, i)) {
                    newArrayList.add(next);
                }
                i++;
            }
            return from(newArrayList);
        }
    }

    public final Cube<E> orderBy() {
        List<E> list = toList();
        Object[] array = list.toArray();
        Arrays.sort(array);
        ListIterator<E> listIterator = list.listIterator();
        for (Object obj : array) {
            listIterator.next();
            listIterator.set((E) obj);
        }
        return from(list);
    }

    public final Cube<E> orderBy(final Comparator<E> comparator) {
        if (comparator != null) {
            List<E> list = toList();
            Collections.sort(list, new java.util.Comparator<E>() {
                /* class com.anzu.sdk.Cube.AnonymousClass2 */

                @Override // java.util.Comparator
                public int compare(E e, E e2) {
                    return comparator.compareTo(e, e2);
                }
            });
            return from(list);
        }
        throw new IllegalArgumentException();
    }

    public final Cube<E> each(Predicate<E> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException();
        } else if (!iterator().hasNext()) {
            return emptyCube();
        } else {
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext() && predicate.predicate(it.next(), i)) {
                i++;
            }
            return this;
        }
    }

    public final Cube<E> parallel(Predicate<E> predicate) {
        return parallel(predicate, 5);
    }

    public final Cube<E> parallel(final Predicate<E> predicate, int i) {
        if (predicate == null) {
            throw new IllegalArgumentException();
        } else if (!iterator().hasNext()) {
            return emptyCube();
        } else {
            ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(Math.max(1, i));
            final Content content = new Content();
            final Iterator<E> it = iterator();
            int i2 = 0;
            while (it.hasNext()) {
                int finalI = i2;
                newFixedThreadPool.submit(new Runnable() {
                    /* class com.anzu.sdk.Cube.AnonymousClass3 */

                    public void run() {
                        content.value(Boolean.valueOf(((Boolean) content.value()).booleanValue() & predicate.predicate(it.next(), finalI)));
                    }
                });
                if (!((Boolean) content.value()).booleanValue()) {
                    break;
                }
                i2++;
            }
            newFixedThreadPool.shutdown();
            return this;
        }
    }

    public final <O> Cube<O> cast(Class<O> cls) {
        if (cls != null) {
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                newArrayList.add(cls.cast(it.next()));
            }
            return from(newArrayList);
        }
        throw new IllegalArgumentException();
    }

    public final <O> Cube<O> ofType(Class<O> cls) {
        if (cls != null) {
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                E next = it.next();
                if (next != null && cls.isAssignableFrom(next.getClass())) {
                    newArrayList.add(cls.cast(next));
                }
            }
            return from(newArrayList);
        }
        throw new IllegalArgumentException();
    }

    public final <O> Cube<O> select(Convertible<E, O> convertible) {
        if (convertible == null) {
            throw new IllegalArgumentException();
        } else if (!iterator().hasNext()) {
            return emptyCube();
        } else {
            int i = 0;
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                newArrayList.add(convertible.transform(it.next(), i));
                i++;
            }
            return from(newArrayList);
        }
    }

    public final <O> Map<O, Cube<E>> group(Classify<E, O> classify) {
        if (classify == null) {
            throw new IllegalArgumentException();
        } else if (!iterator().hasNext()) {
            return emptyMap();
        } else {
            HashMap newHashMap = newHashMap();
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                E next = it.next();
                O groupBy = classify.groupBy(next, i);
                if (newHashMap.containsKey(groupBy)) {
                    ((Cube) newHashMap.get(groupBy)).toList().add(next);
                } else {
                    newHashMap.put(groupBy, from(next));
                }
                i++;
            }
            return newHashMap;
        }
    }

    public final <O> Cube<O> many(Convertible<E, Iterable<O>> convertible) {
        if (convertible != null) {
            Cube<O> emptyCube = emptyCube();
            Iterator<E> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                emptyCube = emptyCube.concat(convertible.transform(it.next(), i));
                i++;
            }
            return emptyCube;
        }
        throw new IllegalArgumentException();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v2, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    public final Cube<Cube<E>> split(int i) {
        if (i >= 1) {
            double size = (double) size(this);
            Double.isNaN(size);
            double d = (double) i;
            Double.isNaN(d);
            int ceil = (int) Math.ceil((size * 1.0d) / d);
            ArrayList newArrayList = newArrayList(new Cube[0]);
            Iterator<E> it = iterator();
            ArrayList newArrayList2 = newArrayList(new Object[0]);
            while (it.hasNext()) {
                newArrayList2.add(it.next());
                if (newArrayList2.size() + 1 > ceil) {
                    newArrayList.add(from(newArrayList2));
                    newArrayList2 = newArrayList(new Object[0]);
                }
            }
            if (newArrayList2.size() > 0) {
                newArrayList.add(from(newArrayList2));
            }
            return from(newArrayList);
        }
        throw new IllegalArgumentException();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    public final Cube<Cube<E>> split(double d) {
        if (d >= 1.0d) {
            ArrayList newArrayList = newArrayList(new Cube[0]);
            Iterator<E> it = iterator();
            ArrayList newArrayList2 = newArrayList(new Object[0]);
            while (it.hasNext()) {
                newArrayList2.add(it.next());
                if (((double) (newArrayList2.size() + 1)) > d) {
                    newArrayList.add(from(newArrayList2));
                    newArrayList2 = newArrayList(new Object[0]);
                }
            }
            if (newArrayList2.size() > 0) {
                newArrayList.add(from(newArrayList2));
            }
            return from(newArrayList);
        }
        throw new IllegalArgumentException();
    }

    public final Cube<E> slice(int i, int i2) {
        int size = size(this);
        if (i > size || (i < 0 && (i = i + size) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (i2 > size || (i2 < 0 && (i2 = i2 + size) < 0)) {
            throw new IndexOutOfBoundsException();
        } else {
            int i3 = 0;
            ArrayList newArrayList = newArrayList(new Object[0]);
            Iterator<E> it = iterator();
            while (it.hasNext() && i3 < i2) {
                E next = it.next();
                if (i3 >= i) {
                    newArrayList.add(next);
                }
                i3++;
            }
            return from(newArrayList);
        }
    }

    @SafeVarargs
    public final Cube<E> intersect(E... eArr) {
        return intersect(asList(eArr));
    }

    @SafeVarargs
    public final Cube<E> intersect(Equality<E> equality, E... eArr) {
        return intersect(asList(eArr), equality);
    }

    public final Cube<E> intersect(Iterable<E> iterable) {
        return intersect(iterable, (Equality) null);
    }

    public final Cube<E> intersect(Iterable<E> iterable, Equality<E> equality) {
        boolean z;
        boolean z2;
        if (iterable == null || !iterable.iterator().hasNext()) {
            return emptyCube();
        }
        ArrayList newArrayList = newArrayList(new Object[0]);
        Iterator<E> it = iterator();
        if (equality == null) {
            while (it.hasNext()) {
                E next = it.next();
                while (true) {
                    z2 = false;
                    for (E e : iterable) {
                        if (z2 || !(next == null || e == null || !next.equals(e))) {
                            z2 = true;
                        }
                    }
                    break;
                }
                if (z2) {
                    newArrayList.add(next);
                }
            }
        } else {
            while (it.hasNext()) {
                E next2 = it.next();
                Iterator<E> it2 = iterable.iterator();
                while (true) {
                    z = false;
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        E next3 = it2.next();
                        if (z || equality.equals(next2, next3)) {
                            z = true;
                        }
                    }
                }
                /*if (z) {
                    newArrayList.add(next2);
                }*/
            }
        }
        return from(newArrayList);
    }

    @SafeVarargs
    public final Cube<E> union(E... eArr) {
        return union(asList(eArr));
    }

    @SafeVarargs
    public final Cube<E> union(Equality<E> equality, E... eArr) {
        return union(asList(eArr), equality);
    }

    public final Cube<E> union(Iterable<E> iterable) {
        return union(iterable, (Equality) null);
    }

    public final Cube<E> union(Iterable<E> iterable, Equality<E> equality) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        if (iterable == null || !iterable.iterator().hasNext()) {
            return this;
        }
        ArrayList newArrayList = newArrayList(new Object[0]);
        List<E> list = intersect(iterable, equality).toList();
        newArrayList.addAll(list);
        if (equality == null) {
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                E next = it.next();
                Iterator<E> it2 = list.iterator();
                while (true) {
                    z4 = false;
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        E next2 = it2.next();
                        if (z4 || next2.equals(next)) {
                            z4 = true;
                        }
                    }
                }
                /*if (!z4) {
                    newArrayList.add(next);
                }*/
            }
            for (E e : iterable) {
                Iterator<E> it3 = list.iterator();
                while (true) {
                    z3 = false;
                    while (true) {
                        if (!it3.hasNext()) {
                            break;
                        }
                        E next3 = it3.next();
                        if (z3 || next3.equals(e)) {
                            z3 = true;
                        }
                    }
                }
                /*if (!z3) {
                    newArrayList.add(e);
                }*/
            }
        } else {
            Iterator<E> it4 = iterator();
            while (it4.hasNext()) {
                E next4 = it4.next();
                Iterator<E> it5 = list.iterator();
                while (true) {
                    z2 = false;
                    while (true) {
                        if (!it5.hasNext()) {
                            break;
                        }
                        E next5 = it5.next();
                        if (z2 || equality.equals(next4, next5)) {
                            z2 = true;
                        }
                    }
                }
                /*if (!z2) {
                    newArrayList.add(next4);
                }*/
            }
            for (E e2 : iterable) {
                Iterator<E> it6 = list.iterator();
                while (true) {
                    z = false;
                    while (true) {
                        if (!it6.hasNext()) {
                            break;
                        }
                        E next6 = it6.next();
                        if (z || equality.equals(e2, next6)) {
                            z = true;
                        }
                    }
                }
                /*if (!z) {
                    newArrayList.add(e2);
                }*/
            }
        }
        return from(newArrayList);
    }

    @SafeVarargs
    public final Cube<E> difference(E... eArr) {
        return difference(asList(eArr));
    }

    @SafeVarargs
    public final Cube<E> difference(Equality<E> equality, E... eArr) {
        return difference(asList(eArr), equality);
    }

    public final Cube<E> difference(Iterable<E> iterable) {
        return difference(iterable, (Equality) null);
    }

    public final Cube<E> difference(Iterable<E> iterable, Equality<E> equality) {
        boolean z;
        boolean z2;
        if (iterable == null || !iterable.iterator().hasNext()) {
            return this;
        }
        ArrayList newArrayList = newArrayList(new Object[0]);
        Cube<E> intersect = intersect(iterable, equality);
        if (equality == null) {
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                E next = it.next();
                Iterator<E> it2 = intersect.iterator();
                while (true) {
                    z2 = false;
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        E next2 = it2.next();
                        if (z2 || next2.equals(next)) {
                            z2 = true;
                        }
                    }
                }
                /*if (!z2) {
                    newArrayList.add(next);
                }*/
            }
        } else {
            Iterator<E> it3 = iterator();
            while (it3.hasNext()) {
                E next3 = it3.next();
                Iterator<E> it4 = intersect.iterator();
                while (true) {
                    z = false;
                    while (true) {
                        if (!it4.hasNext()) {
                            break;
                        }
                        E next4 = it4.next();
                        if (z || equality.equals(next3, next4)) {
                            z = true;
                        }
                    }
                }
                /*if (!z) {
                    newArrayList.add(next3);
                }*/
            }
        }
        return from(newArrayList);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            E next = it.next();
            sb.append(", ");
            sb.append(next == null ? "null" : next.toString());
        }
        if (sb.length() > 1) {
            sb.delete(0, 2);
        }
        StringBuilder insert = sb.insert(0, "[");
        insert.append("]");
        return insert.toString();
    }
}