package src.main;



public class Paire <T, P> {
    private T first;
    private P second;



    public Paire(T t, P p) {
        first = t;
        second = p;
   }


   public T getFirst() {
       return first;
   }


   public P getSecond() {
       return second;
   }


   public void setFirst(T first) {
       this.first = first;
   }


   public void setSecond(P second) {
       this.second = second;
   }
}