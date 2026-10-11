package Entity;

/** A quest definition; progress and money remain authoritative in the Java domain. */
public record Quest(String id,String vi,String en,String detailVi,String detailEn,String metric,int target,int coins,int diamonds,String animal) {}
