1,onCreate��ȡ��������ûص�
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
2,onResume�д򿪺����ò���
hsiScanner.open();
hsiScanner.setParams(SymbologyID.CODE128, 1); // 1����ʾ�򿪸����룬0 ��ʾ�رգ�setParams������open��֮��������ã�
3,onPause�йر�
hsiScanner.close();
onDestory���ͷŶ���
HSIScanner.destroyInstance();