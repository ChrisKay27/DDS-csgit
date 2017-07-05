package utilities ;

import java.util.stream.Stream ;
import java.util.stream.IntStream ;
import java.util.function.BiFunction ;
import java.util.function.Function ;

public class Utilities
{
    public static <C extends Comparable<? super C>> Pair<C,Integer>
    minAndCount(Stream<C> stream)
        {
        int count = 0 ;
        C current = null ;
        stream.forEach(c ->
            {
            if (count==0)
                {
                count = 1 ;
                current = c ;
                }
            else
                {
                int x = c.compareTo(current) ;
                if (x<0)
                    {
                    count = 1 ;
                    current = c ;
                    }
                else if (x==0)
                    ++count ;
                }
            }) ;
        return new Pair<C,Integer>(current,count) ;
        }
    
    public static Pair<Integer,Integer>
    minAndCount(IntStream stream)
        {
        int count = 0 ;
        Integer current = Integer.MAX_VALUE ; // rhs irrelevant!
        stream.forEach(c ->
                       {
                       if (count==0)
                           {
                           count = 1 ;
                           current = c ;
                           }
                       else
                           {
                           int x = c-current ;
                           if (x<0)
                               {
                               count = 1 ;
                               current = c ;
                               }
                           else if (x==0)
                               ++count ;
                           }
                       }) ;
        return new Pair<Integer,Integer>(current,count) ;
        }

    public static <A,B,C> BiFunction<B,A,C> flip(BiFunction<A,B,C> f)
        {
        return (b,a) -> f.apply(a,b) ;
        }
    
    public static <A,B,C> Function<A,Function<B,C>> curry(BiFunction<A,B,C> f)
        {
        return a -> b  -> f.apply(a,b) ;
        }
    
    public static <A,C> Function<Function<A,C>,C> at(A x)
        {
        return f -> f.apply(x) ;
        }

}
