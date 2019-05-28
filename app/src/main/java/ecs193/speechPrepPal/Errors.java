package ecs193.speechPrepPal;

public class Errors {
    int scriptStart;
    int scriptEnd;
    int speechStart;
    int speechEnd;

    Errors(int scriptStart, int scriptEnd, int speechStart, int speechEnd)
    {
        this.scriptStart = scriptStart;
        this.scriptEnd = scriptEnd;
        this.speechStart = speechStart;
        this.speechEnd = speechEnd;
    }

    public String toString()
    {
        return "script: " + scriptStart + "-" +scriptEnd + "\t speech: "+ speechStart+"-"+speechEnd;
    }
}
