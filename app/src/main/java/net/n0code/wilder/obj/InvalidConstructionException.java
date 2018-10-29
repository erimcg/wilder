package net.n0code.wilder.obj;

@SuppressWarnings("serial")
public class InvalidConstructionException extends Exception{

    String mssg = null;

    public InvalidConstructionException(String mssg)
    {
        this.mssg = mssg;
    }

    @Override
    public String toString( )
    {
        return mssg;
    }
}
