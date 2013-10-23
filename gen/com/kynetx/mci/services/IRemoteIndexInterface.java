/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/pbs/android/workspace/MobileCloudIndex/MCI Android/src/com/kynetx/mci/services/IRemoteIndexInterface.aidl
 */
package com.kynetx.mci.services;
public interface IRemoteIndexInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.kynetx.mci.services.IRemoteIndexInterface
{
private static final java.lang.String DESCRIPTOR = "com.kynetx.mci.services.IRemoteIndexInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.kynetx.mci.services.IRemoteIndexInterface interface,
 * generating a proxy if needed.
 */
public static com.kynetx.mci.services.IRemoteIndexInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.kynetx.mci.services.IRemoteIndexInterface))) {
return ((com.kynetx.mci.services.IRemoteIndexInterface)iin);
}
return new com.kynetx.mci.services.IRemoteIndexInterface.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getMediaIndex:
{
data.enforceInterface(DESCRIPTOR);
com.kynetx.mci.models.MediaIndex _result = this.getMediaIndex();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getJson:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getJson();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_stopService:
{
data.enforceInterface(DESCRIPTOR);
this.stopService();
reply.writeNoException();
return true;
}
case TRANSACTION_getMediaList:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.kynetx.mci.models.MediaIndex> _result = this.getMediaList();
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_doWeHaveMedia:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.doWeHaveMedia();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.kynetx.mci.services.IRemoteIndexInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public com.kynetx.mci.models.MediaIndex getMediaIndex() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.kynetx.mci.models.MediaIndex _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getMediaIndex, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.kynetx.mci.models.MediaIndex.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getJson() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getJson, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void stopService() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopService, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.util.List<com.kynetx.mci.models.MediaIndex> getMediaList() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<com.kynetx.mci.models.MediaIndex> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getMediaList, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(com.kynetx.mci.models.MediaIndex.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean doWeHaveMedia() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_doWeHaveMedia, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getMediaIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getJson = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_stopService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getMediaList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_doWeHaveMedia = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public com.kynetx.mci.models.MediaIndex getMediaIndex() throws android.os.RemoteException;
public java.lang.String getJson() throws android.os.RemoteException;
public void stopService() throws android.os.RemoteException;
public java.util.List<com.kynetx.mci.models.MediaIndex> getMediaList() throws android.os.RemoteException;
public boolean doWeHaveMedia() throws android.os.RemoteException;
}
