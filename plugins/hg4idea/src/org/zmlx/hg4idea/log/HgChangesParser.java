// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.zmlx.hg4idea.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcs.log.VcsShortCommitDetails;
import com.intellij.vcs.log.impl.VcsChangesLazilyParsedDetails;
import com.intellij.vcs.log.impl.VcsFileStatusInfo;
import org.jetbrains.annotations.NotNull;
import org.zmlx.hg4idea.HgRevisionNumber;
import org.zmlx.hg4idea.provider.HgChangeProvider;

import java.util.List;

import static org.zmlx.hg4idea.log.HgHistoryUtil.createChange;

class HgChangesParser implements VcsChangesLazilyParsedDetails.ChangesParser {
  @NotNull private final HgRevisionNumber myRevisionNumber;

  HgChangesParser(@NotNull HgRevisionNumber revisionNumber) {
    myRevisionNumber = revisionNumber;
  }

  @Override
  public List<Change> parseStatusInfo(@NotNull Project project,
                                      @NotNull VcsShortCommitDetails commit,
                                      @NotNull List<VcsFileStatusInfo> changes,
                                      int parentIndex) {
    List<Change> result = ContainerUtil.newArrayList();
    for (VcsFileStatusInfo info : changes) {
      String filePath = info.getFirstPath();
      HgRevisionNumber parentRevision =
        myRevisionNumber.getParents().isEmpty() ? null : myRevisionNumber.getParents().get(parentIndex);
      switch (info.getType()) {
        case MODIFICATION:
          result.add(createChange(project, commit.getRoot(), filePath, parentRevision, filePath, myRevisionNumber, FileStatus.MODIFIED));
          break;
        case NEW:
          result.add(createChange(project, commit.getRoot(), null, null, filePath, myRevisionNumber, FileStatus.ADDED));
          break;
        case DELETED:
          result.add(createChange(project, commit.getRoot(), filePath, parentRevision, null, myRevisionNumber, FileStatus.DELETED));
          break;
        case MOVED:
          result.add(createChange(project, commit.getRoot(), filePath, parentRevision, info.getSecondPath(), myRevisionNumber,
                                  HgChangeProvider.RENAMED));
          break;
      }
    }
    return result;
  }
}
