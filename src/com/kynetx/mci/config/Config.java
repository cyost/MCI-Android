package com.kynetx.mci.config;

public class Config {
	
	public static final boolean IS_DEBUG = true;
	public static final boolean CREATE_TABLES = true;
	public static final String CLOUD_OS_CLIENT_ID = "40187FA4-093B-11E3-A922-1BBD3D33CA1D";
	public static final String CLOUD_OS_APP_CODE = "b177052x7";
	public static final String APP_URL = "https://cs.kobj.net/oauth/authorize?response_type=code&redirect_uri=https%3A%2F%2Fsquaretag.com&client_id=40187FA4-093B-11E3-A922-1BBD3D33CA1D&state=6wO9eq5EPP";
	public static final String CALL_BACK_URL = "https://squaretag.com";
	public static final String OAUTH_CODE = "ZDI1NDkwYjQ1OTAyNjFhNTVjMjQxZWY5MjQxOWZmYWFhNjNiNjNhMDM1NzYyZjM5YTU4Y2U4OWQ5ZTQwMmE3M2RmODY3OWMwMThkNzNkZDliOGY4ZTdmOTNjZjJmYjExM2FlZGZkYTMyMGUxNWQyZWNiNzU4NDg4MjY0YjMwZWFiNmNmZGQwOTc0OTQ4NTFiODdkZWI3ZjcyNzVhZmQ1YmQ3YTNlY2YzYzQxNzYwOWMxZjBiMWJmZDY3MzU4OGQzZTYwYTA3ZDIwYWRiYTljNmYyNmFkODFjZGExZjFiMDg4YjAwNjYwZWE1N2RiNDUwYjUzMjMwNmEwOWRjNjI3Yg&state=6wO9eq5EPP";
	public static final String URL_TO_GET_OAUTH = "https://squaretag.com/oauth.html#!/app/b177052x7/oauth_authorize&developer_eci=40187FA4-093B-11E3-A922-1BBD3D33CA1D&client_state=6wO9eq5EPP&uri_redirect=https%3A%2F%2Fsquaretag.com";

	public static final String GET_MEDIA_LIST_URL = "https://cs.kobj.net/sky/cloud/a169x727/mciListMedia";
	public static final String GET_MEDIA_PLAY_LIST_URL = "https://cs.kobj.net/sky/cloud/a169x727/mciMediaPlayList";
	public static final String HEADER_KOBJ = "Kobj-Session";
	public static final String HOST = "cs.kobj.net";
	
	//public static final String GUID_TO_GET_DEVICE_LIST = "A2E3CC48-09EE-11E3-A275-7C5C1257AE36"; //Dev
	//public static final String GUID_TO_GET_DEVICE_LIST = "1FCEA696-230E-11E3-A7AA-D6A7E71C24E1"; //For Neustar Client
	public static final int MEDIA_UPLOAD_LIMIT = 5;
	public static final String EID = "51236986";
	
	//public static final String ANDROID_DEVICE_CHANNEL = "5D73FA42-09EF-11E3-AB7F-02089790E4B9";
	/*
	public static final String LAPTOP_DEVICE_CHANNEL = "A72000BE-09EF-11E3-B966-7FF0E71C24E1";
	public static final String IOS_DEVICE_CHANNEL = "18A2DD70-09EF-11E3-92CF-9596E71C24E1";*/
	public static final String testflightKey = "96207991-cd3e-4324-99bf-bcb1ae8eeb2d";
	public static String deviceId;
	public static String deviceName;
	public static boolean mediaPlaying = false;
	public static boolean startDone = false;
	
	
}
