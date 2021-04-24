package test.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import data.ProgrammData;
import data.ProgrammData.Version;

public class ProgramDataTest {
    @Test
    public void checkVersionSimilarity() {
        List<Version> versions = ProgrammData.getLocalVersionList();
        long maxVersion = -1;
        for (int i = 0; i < versions.size(); ++i) {
            Version v = versions.get(i);
            long c = ProgrammData.getLongOfVersion(v.code);
            maxVersion = Math.max(maxVersion, c);
        }
        assertEquals(maxVersion, ProgrammData.getLongOfVersion(ProgrammData.getVersion()));
    }
}
