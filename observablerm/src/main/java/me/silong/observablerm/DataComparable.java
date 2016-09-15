package me.silong.observablerm;

public interface DataComparable<D> {

  boolean areContentsTheSame(D oldData, D newData);

  boolean areItemsTheSame(D oldData, D newData);
}