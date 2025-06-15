import 'dart:io';

import 'app_device_integrity_platform_interface.dart';

class AppDeviceIntegrity {
  Future<String?> getAttestationServiceSupport({
    required String challenge,
    int? gcp,
    bool classic = false,
  }) {
    if (Platform.isAndroid) {
      return AppDeviceIntegrityPlatform.instance.getAttestationServiceSupport(
        challengeString: challenge,
        gcp: gcp!,
        classic: classic,
      );
    }

    return AppDeviceIntegrityPlatform.instance.getAttestationServiceSupport(
      challengeString: challenge,
    );
  }
}
