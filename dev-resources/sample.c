a();
#if COND1 \
+1
b();
#elif test
c();
#else
d();
#endif
e("f");
f("c");
printf("a");
printf("b");
