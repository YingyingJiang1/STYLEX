
@Override
public void onBroadcastReceived(Intent intent) {
    super.onBroadcastReceived(intent);
    if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
        closeItemsList();
    } else if (ACTION_PLAY_PAUSE.equals(intent.getAction())) {
        if (!fragmentIsVisible) {
            onFragmentStopped();
        }
    } else if (VideoDetailFragment.ACTION_VIDEO_FRAGMENT_STOPPED.equals(intent.getAction())) {
        fragmentIsVisible = false;
        onFragmentStopped();
    } else if (VideoDetailFragment.ACTION_VIDEO_FRAGMENT_RESUMED.equals(intent.getAction())) {
        fragmentIsVisible = true;
        player.useVideoSource(true);
        if (!isControlsVisible()) {
            hideSystemUIIfNeeded();
        }
    }
}
