package utilities ;

import java.util.function.Function ;

@FunctionalInterface
public interface Function11<A,B,C,D,E,F,G,H,I,J,K,L>
{
    public abstract L apply (A a,B b,C c,D d,E e,F f,G g,H h,I i,J j,K k) ;
    default
    Function<A,
    Function<B,
    Function<C,
    Function<D,
    Function<E,
    Function<F,
    Function<G,
    Function<H,
    Function<I,
    Function<J,
    Function<K,L> > > > > > > > > > > curry()
    {
    return a -> b -> c -> d -> e -> f -> g -> h -> i -> j -> k -> apply(a,b,c,d,e,f,g,h,i,j,k) ;
    }
}
