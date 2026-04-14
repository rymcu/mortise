package com.rymcu.mortise.voice.kernel.catalog;

import java.util.List;

/**
 * 语音目录贡献者。
 */
public interface VoiceCatalogContributor {

    List<VoiceCatalogContribution> contribute();
}
