1,onCreate获取对象和设置回调
hsiScanner = HSIScanner.getInstance(this, surfaceView);
hsiScanner.setCompleteCallback(this);

	@Override
	public void onComplete(CommodityInfo commodityInfo) {
		if (commodityInfo != null) {
			String[] strings = commodityInfo.getbarcodeArr();
			for (int i = 0; i < strings.length; i++) {
				SeuicLog.d("barcode:" + strings[i]);
			}
		}
	}
2,onResume中打开和设置参数
hsiScanner.open();
hsiScanner.setParams(SymbologyID.CODE128, 1); // 1，表示打开该条码，0 表示关闭（setParams必须在open的之后才能设置）
3,onPause中关闭
hsiScanner.close();
onDestory中释放对象
HSIScanner.destroyInstance();