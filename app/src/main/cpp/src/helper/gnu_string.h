#pragma once
#include <string>

// This code taken from gcc-11.3.0 (gcc-12 doesn't seem to be using the
// COW Reference calculated string implementation anymore).
// I just took some necessary code only.

// https://github.com/gcc-mirror/gcc/blob/releases/gcc-11.3.0/libstdc++-v3/include/bits/basic_string.h
// https://github.com/gcc-mirror/gcc/blob/releases/gcc-11.3.0/libstdc++-v3/include/bits/basic_string.tcc


namespace gnu {
// Reference-counted COW string implentation

/**
 *  @class basic_string basic_string.h <string>
 *  @brief  Managing sequences of characters and character-like objects.
 *
 *  @ingroup strings
 *  @ingroup sequences
 *
 *  @tparam _CharT  Type of character
 *  @tparam _Traits  Traits for character type, defaults to
 *                   char_traits<_CharT>.
 *  @tparam _Alloc  Allocator type, defaults to allocator<_CharT>.
 *
 *  Meets the requirements of a <a href="tables.html#65">container</a>, a
 *  <a href="tables.html#66">reversible container</a>, and a
 *  <a href="tables.html#67">sequence</a>.  Of the
 *  <a href="tables.html#68">optional sequence requirements</a>, only
 *  @c push_back, @c at, and @c %array access are supported.
 *
 *  @doctodo
 *
 *
 *  Documentation?  What's that?
 *  Nathan Myers <ncm@cantrip.org>.
 *
 *  A string looks like this:
 *
 *  @code
 *                                        [_Rep]
 *                                        _M_length
 *   [basic_string<char_type>]            _M_capacity
 *   _M_dataplus                          _M_refcount
 *   _M_p ---------------->               unnamed array of char_type
 *  @endcode
 *
 *  Where the _M_p points to the first character in the string, and
 *  you cast it to a pointer-to-_Rep and subtract 1 to get a
 *  pointer to the header.
 *
 *  This approach has the enormous advantage that a string object
 *  requires only one allocation.  All the ugliness is confined
 *  within a single %pair of inline functions, which each compile to
 *  a single @a add instruction: _Rep::_M_data(), and
 *  string::_M_rep(); and the allocation function which gets a
 *  block of raw bytes and with room enough and constructs a _Rep
 *  object at the front.
 *
 *  The reason you want _M_data pointing to the character %array and
 *  not the _Rep is so that the debugger can see the string
 *  contents. (Probably we should add a non-inline member to get
 *  the _Rep for the debugger to use, so users can check the actual
 *  string length.)
 *
 *  Note that the _Rep object is a POD so that you can have a
 *  static <em>empty string</em> _Rep object already @a constructed before
 *  static constructors have run.  The reference-count encoding is
 *  chosen so that a 0 indicates one reference, so you never try to
 *  destroy the empty-string _Rep object.
 *
 *  All but the last paragraph is considered pretty conventional
 *  for a C++ string implementation.
*/
// 21.3  Template class basic_string
template<typename _CharT, typename _Traits, typename _Alloc>
class basic_string
{
    typedef typename _Alloc::template
        rebind<_CharT>::other _CharT_alloc_type;
    typedef std::allocator_traits<_CharT_alloc_type> _CharT_alloc_traits;

    // Types:
public:
    typedef _Traits                                      traits_type;
    typedef _Alloc                                       allocator_type;
    typedef typename _CharT_alloc_traits::size_type      size_type;

    // NB: This is an unsigned type, and thus represents the maximum
    // size that the allocator can hold.
    ///  Value returned by various member functions when they fail.
    static const size_type	npos = static_cast<size_type>(-1);

private:
    // _Rep: string representation
    //   Invariants:
    //   1. String really contains _M_length + 1 characters: due to 21.3.4
    //      must be kept null-terminated.
    //   2. _M_capacity >= _M_length
    //      Allocated memory is always (_M_capacity + 1) * sizeof(_CharT).
    //   3. _M_refcount has three states:
    //      -1: leaked, one reference, no ref-copies allowed, non-const.
    //       0: one reference, non-const.
    //     n>0: n + 1 references, operations require a lock, const.
    //   4. All fields==0 is an empty string, given the extra storage
    //      beyond-the-end for a null terminator; thus, the shared
    //      empty string representation needs no constructor.

    struct _Rep_base
    {
        size_type _M_length;
        size_type _M_capacity;
        std::atomic<int> _M_refcount;
    };

    struct _Rep : _Rep_base
    {
        // Types:
        typedef typename _Alloc::template rebind<char>::other _Raw_bytes_alloc;

        // (Public) Data members:

        // The maximum number of individual char_type elements of an
        // individual string is determined by _S_max_size. This is the
        // value that will be returned by max_size().  (Whereas npos
        // is the maximum number of bytes the allocator can allocate.)
        // If one was to divvy up the theoretical largest size string,
        // with a terminating character and m _CharT elements, it'd
        // look like this:
        // npos = sizeof(_Rep) + (m * sizeof(_CharT)) + sizeof(_CharT)
        // Solving for m:
        // m = ((npos - sizeof(_Rep))/sizeof(CharT)) - 1
        // In addition, this implementation quarters this amount.
        static const size_type	_S_max_size = (((npos - sizeof(_Rep_base))/sizeof(_CharT)) - 1) / 4;
        static const _CharT _S_terminal = _CharT();

        void
        _M_set_sharable() noexcept
        { this->_M_refcount = 0; }

        void
        _M_set_length_and_sharable(size_type __n) noexcept
        {
            this->_M_set_sharable();  // One reference.
            this->_M_length = __n;
            // traits_type::assign(this->_M_refdata()[__n], _S_terminal);
            traits_type::assign(this->_M_refdata()[__n], '\0');
            // grrr. (per 21.3.4)
            // You cannot leave those LWG people alone for a second.
        }

        _CharT*
        _M_refdata() throw()
        { return reinterpret_cast<_CharT*>(this + 1); }

        // Create & Destroy
        static _Rep*
        _S_create(size_type __capacity, size_type __old_capacity,
                  const _Alloc& __alloc)
        {
            // _GLIBCXX_RESOLVE_LIB_DEFECTS
            // 83.  String::npos vs. string::max_size()
            if (__capacity > _S_max_size)
                throw "basic_string::_S_create";

            // The standard places no restriction on allocating more memory
            // than is strictly needed within this layer at the moment or as
            // requested by an explicit application call to reserve(n).

            // Many malloc implementations perform quite poorly when an
            // application attempts to allocate memory in a stepwise fashion
            // growing each allocation size by only 1 char.  Additionally,
            // it makes little sense to allocate less linear memory than the
            // natural blocking size of the malloc implementation.
            // Unfortunately, we would need a somewhat low-level calculation
            // with tuned parameters to get this perfect for any particular
            // malloc implementation.  Fortunately, generalizations about
            // common features seen among implementations seems to suffice.

            // __pagesize need not match the actual VM page size for good
            // results in practice, thus we pick a common value on the low
            // side.  __malloc_header_size is an estimate of the amount of
            // overhead per memory allocation (in practice seen N * sizeof
            // (void*) where N is 0, 2 or 4).  According to folklore,
            // picking this value on the high side is better than
            // low-balling it (especially when this algorithm is used with
            // malloc implementations that allocate memory blocks rounded up
            // to a size which is a power of 2).
            const size_type __pagesize = 4096;
            const size_type __malloc_header_size = 4 * sizeof(void*);

            // The below implements an exponential growth policy, necessary to
            // meet amortized linear time requirements of the library: see
            // http://gcc.gnu.org/ml/libstdc++/2001-07/msg00085.html.
            // It's active for allocations requiring an amount of memory above
            // system pagesize. This is consistent with the requirements of the
            // standard: http://gcc.gnu.org/ml/libstdc++/2001-07/msg00130.html
            if (__capacity > __old_capacity && __capacity < 2 * __old_capacity)
                __capacity = 2 * __old_capacity;

            // NB: Need an array of char_type[__capacity], plus a terminating
            // null char_type() element, plus enough for the _Rep data structure.
            // Whew. Seemingly so needy, yet so elemental.
            size_type __size = (__capacity + 1) * sizeof(_CharT) + sizeof(_Rep);

            const size_type __adj_size = __size + __malloc_header_size;
            if (__adj_size > __pagesize && __capacity > __old_capacity)
            {
                const size_type __extra = __pagesize - __adj_size % __pagesize;
                __capacity += __extra / sizeof(_CharT);
                // Never allocate a string bigger than _S_max_size.
                if (__capacity > _S_max_size)
                    __capacity = _S_max_size;
                __size = (__capacity + 1) * sizeof(_CharT) + sizeof(_Rep);
            }

            // NB: Might throw, but no worries about a leak, mate: _Rep()
            // does not throw.
            void* __place = _Raw_bytes_alloc(__alloc).allocate(__size);
            _Rep *__p = new (__place) _Rep;
            __p->_M_capacity = __capacity;
            // ABI compatibility - 3.4.x set in _S_create both
            // _M_refcount and _M_length.  All callers of _S_create
            // in basic_string.tcc then set just _M_length.
            // In 4.0.x and later both _M_refcount and _M_length
            // are initialized in the callers, unfortunately we can
            // have 3.4.x compiled code with _S_create callers inlined
            // calling 4.0.x+ _S_create.
            __p->_M_set_sharable();
            return __p;
        }

        void
        _M_dispose(const _Alloc& __a) noexcept
        {
#define _GLIBCXX_SYNCHRONIZATION_HAPPENS_BEFORE(A) A.wait()
#define _GLIBCXX_SYNCHRONIZATION_HAPPENS_AFTER(A) A.notify_all()

            // Be race-detector-friendly.  For more info see bits/c++config.
            _GLIBCXX_SYNCHRONIZATION_HAPPENS_BEFORE(&this->_M_refcount);
            // Decrement of _M_refcount is acq_rel, because:
            // - all but last decrements need to release to synchronize with
            //   the last decrement that will delete the object.
            // - the last decrement needs to acquire to synchronize with
            //   all the previous decrements.
            // - last but one decrement needs to release to synchronize with
            //   the acquire load in _M_is_shared that will conclude that
            //   the object is not shared anymore.
            // if (__gnu_cxx::__exchange_and_add_dispatch(&this->_M_refcount,
            //                                           -1) <= 0)
            // {
            //     _GLIBCXX_SYNCHRONIZATION_HAPPENS_AFTER(&this->_M_refcount);
            //     _M_destroy(__a);
            // }
            if (this->_M_refcount.exchange(-1) <= 0) {
                _GLIBCXX_SYNCHRONIZATION_HAPPENS_AFTER(&this->_M_refcount);
                _M_destroy(__a);
            }
        }  // XXX MT

        void
        _M_destroy(const _Alloc& __a) throw()
        {
            const size_type __size = sizeof(_Rep_base) +
                                     (this->_M_capacity + 1) * sizeof(_CharT);
            _Raw_bytes_alloc(__a).deallocate(reinterpret_cast<char*>(this), __size);
        }
    };

    // Use empty-base optimization: http://www.cantrip.org/emptyopt.html
    struct _Alloc_hider : _Alloc
    {
        _Alloc_hider(_CharT* __dat, const _Alloc& __a) noexcept
            : _Alloc(__a), _M_p(__dat) { }

        _CharT* _M_p; // The actual data.
    };

private:
    // Data Members (private):
    mutable _Alloc_hider	_M_dataplus;

    _CharT*
    _M_data() const noexcept
    { return  _M_dataplus._M_p; }

    _CharT*
    _M_data(_CharT* __p) noexcept
    { return (_M_dataplus._M_p = __p); }

    _Rep* _M_rep() const noexcept
    { return &((reinterpret_cast<_Rep*> (_M_data()))[-1]); }

    static void
    _M_assign(_CharT* __d, size_type __n, _CharT __c) noexcept
    {
        if (__n == 1)
            traits_type::assign(*__d, __c);
        else
            traits_type::assign(__d, __n, __c);
    }

    // _S_copy_chars is a separate template to permit specialization
    // to optimize for the common case of pointers as iterators.
    template<class _Iterator>
    static void
    _S_copy_chars(_CharT* __p, _Iterator __k1, _Iterator __k2)
    {
        for (; __k1 != __k2; ++__k1, (void)++__p)
            traits_type::assign(*__p, *__k1); // These types are off.
    }

public:
    // Construct/copy/destroy:
    // NB: We overload ctors in some cases instead of using default
    // arguments, per 17.4.4.4 para. 2 item 2.

    /**
     *  @brief  Default constructor creates an empty string.
     */
    basic_string()
        : _M_dataplus(_S_construct(size_type(), _CharT(), _Alloc()), _Alloc()){ }

    /**
     *  @brief  Construct string initialized by a character %array.
     *  @param  __s  Source character %array.
     *  @param  __n  Number of characters to copy.
     *  @param  __a  Allocator to use (default is default allocator).
     *
     *  NB: @a __s must have at least @a __n characters, &apos;\\0&apos;
     *  has no special meaning.
     */
    basic_string(const _CharT* __s, size_type __n,
        const _Alloc& __a = _Alloc())
        : _M_dataplus(_S_construct(__s, __s + __n, __a), __a) { }

    /**
     *  @brief  Construct string as copy of a C string.
     *  @param  __s  Source C string.
     *  @param  __a  Allocator to use (default is default allocator).
     */
    basic_string(const _CharT* __s, const _Alloc& __a = _Alloc())
        : _M_dataplus(_S_construct(__s, __s ? __s + traits_type::length(__s) :
                                    __s + npos, __a), __a) { }

    /**
     *  @brief  Destroy the string instance.
     */
    ~basic_string() noexcept
    { _M_rep()->_M_dispose(this->get_allocator()); }

public:
    // Capacity:
    ///  Returns the number of characters in the string, not including any
    ///  null-termination.
    size_type
    size() const noexcept
    { return _M_rep()->_M_length; }

    ///  Returns the number of characters in the string, not including any
    ///  null-termination.
    size_type
    length() const noexcept
    { return _M_rep()->_M_length; }

    ///  Returns the size() of the largest possible %string.
    size_type
    max_size() const noexcept
    { return _Rep::_S_max_size; }

    /**
     *  Returns the total number of characters that the %string can hold
     *  before needing to allocate more memory.
     */
    size_type
    capacity() const noexcept
    { return _M_rep()->_M_capacity; }

    /**
     *  Returns true if the %string is empty.  Equivalent to
     *  <code>*this == ""</code>.
     */
    [[nodiscard]] bool
    empty() const noexcept
    { return this->size() == 0; }

private:
    // _S_construct_aux is used to implement the 21.3.1 para 15 which
    // requires special behaviour if _InIter is an integral type
    template<class _InIterator>
    static _CharT*
    _S_construct_aux(_InIterator __beg, _InIterator __end,
                     const _Alloc& __a, std::false_type)
    {
        typedef typename std::iterator_traits<_InIterator>::iterator_category _Tag;
        return _S_construct(__beg, __end, __a, _Tag());
    }

    // _GLIBCXX_RESOLVE_LIB_DEFECTS
    // 438. Ambiguity in the "do the right thing" clause
    template<class _Integer>
    static _CharT*
    _S_construct_aux(_Integer __beg, _Integer __end,
                     const _Alloc& __a, std::true_type)
    { return _S_construct_aux_2(static_cast<size_type>(__beg),
                                __end, __a); }

    static _CharT*
    _S_construct_aux_2(size_type __req, _CharT __c, const _Alloc& __a)
    { return _S_construct(__req, __c, __a); }

    template<class _InIterator>
    static _CharT*
    _S_construct(_InIterator __beg, _InIterator __end, const _Alloc& __a)
    {
        typedef typename std::is_integral<_InIterator>::type _Integral;
        return _S_construct_aux(__beg, __end, __a, _Integral());
    }

    // For Input Iterators, used in istreambuf_iterators, etc.
    template<class _InIterator>
    static _CharT*
    _S_construct(_InIterator __beg, _InIterator __end, const _Alloc& __a,
                 std::input_iterator_tag)
    {
        // Avoid reallocation for common case.
        _CharT __buf[128];
        size_type __len = 0;
        while (__beg != __end && __len < sizeof(__buf) / sizeof(_CharT))
        {
            __buf[__len++] = *__beg;
            ++__beg;
        }
        _Rep* __r = _Rep::_S_create(__len, size_type(0), __a);
        _M_copy(__r->_M_refdata(), __buf, __len);
        try
        {
            while (__beg != __end)
            {
                if (__len == __r->_M_capacity)
                {
                    // Allocate more space.
                    _Rep* __another = _Rep::_S_create(__len + 1, __len, __a);
                    _M_copy(__another->_M_refdata(), __r->_M_refdata(), __len);
                    __r->_M_destroy(__a);
                    __r = __another;
                }
                __r->_M_refdata()[__len++] = *__beg;
                ++__beg;
            }
        }
        catch(...)
        {
            __r->_M_destroy(__a);
            throw;
        }
        __r->_M_set_length_and_sharable(__len);
        return __r->_M_refdata();
    }

    // For forward_iterators up to random_access_iterators, used for
    // string::iterator, _CharT*, etc.
    template<class _FwdIterator>
    static _CharT*
    _S_construct(_FwdIterator __beg, _FwdIterator __end, const _Alloc& __a,
                 std::forward_iterator_tag)
    {
        // NB: Not required, but considered best practice.
        if (__beg == nullptr && __beg != __end)
            throw "basic_string::_S_construct null not valid";

        const size_type __dnew = static_cast<size_type>(std::distance(__beg,
                                                                      __end));
        // Check for out_of_range and length_error exceptions.
        _Rep* __r = _Rep::_S_create(__dnew, size_type(0), __a);
        try
        { _S_copy_chars(__r->_M_refdata(), __beg, __end); }
        catch(...)
        {
            __r->_M_destroy(__a);
            throw;
        }
        __r->_M_set_length_and_sharable(__dnew);
        return __r->_M_refdata();
    }

    static _CharT*
    _S_construct(size_type __n, _CharT __c, const _Alloc& __a)
    {
        // Check for out_of_range and length_error exceptions.
        _Rep* __r = _Rep::_S_create(__n, size_type(0), __a);
        if (__n)
            _M_assign(__r->_M_refdata(), __n, __c);

        __r->_M_set_length_and_sharable(__n);
        return __r->_M_refdata();
    }

public:
    // String operations:
    /**
     *  @brief  Return const pointer to null-terminated contents.
     *
     *  This is a handle to internal data.  Do not modify or dire things may
     *  happen.
     */
    const _CharT*
    c_str() const noexcept
    { return _M_data(); }

    /**
     *  @brief  Return const pointer to contents.
     *
     *  This is a pointer to internal data.  It is undefined to modify
     *  the contents through the returned pointer. To get a pointer that
     *  allows modifying the contents use @c &str[0] instead,
     *  (or in C++17 the non-const @c str.data() overload).
     */
    const _CharT*
    data() const noexcept
    { return _M_data(); }

#if __cplusplus > 201703L
    /**
     *  @brief  Return non-const pointer to contents.
     *
     *  This is a pointer to the character sequence held by the string.
     *  Modifying the characters in the sequence is allowed.
     */
    _CharT*
    data() noexcept
    { return _M_data(); }
#endif // C++17

    /**
     *  @brief  Return copy of allocator used to construct this string.
     */
    allocator_type
    get_allocator() const noexcept
    { return _M_dataplus; }
};

using string = basic_string<char, std::char_traits<char>, std::allocator<char>>;
using wstring = basic_string<wchar_t, std::char_traits<wchar_t>, std::allocator<wchar_t>>;
}
