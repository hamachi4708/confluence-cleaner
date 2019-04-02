package nh321.api;

import java.util.Date;
import java.util.List;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.spaces.Space;

import nh321.rest.AttachmentModel;
import nh321.rest.PageVersionsModel;
import nh321.rest.SpaceTrashModel;
import nh321.rest.SpaceVersionsModel;

public interface DataCleanUtil {
    public abstract Date getCreatedOrUpdatedDate(int paramInt);

    public abstract long removeAttachmentVersions(Attachment paramAttachment, int paramInt);

    public abstract long removePageVersions(AbstractPage paramAbstractPage, int paramInt, String paramString);

    public abstract long removeSpaceVersions(Space paramSpace, int paramInt, String paramString);

    public abstract long removeAllVersions(int paramInt, String paramString);

    public abstract int removeSpaceTrash(Space paramSpace);

    public abstract long removeAllTrash();

    public abstract AttachmentModel getAttachmentVersionSummary(Attachment paramAttachment, int paramInt);

    public abstract PageVersionsModel getPageVersionSummary(Page paramPage, int paramInt, String paramString);

    public abstract SpaceVersionsModel getSpaceVersionSummary(Space paramSpace, int paramInt, String paramString);

    public abstract List<SpaceVersionsModel> getAllVersionSummary(int paramInt, String paramString);

    public abstract SpaceTrashModel getSpaceTrashSummary(Space paramSpace);

    public abstract List<SpaceTrashModel> getAllTrashSummary();

    public abstract void setLimit(int paramInt);

    public abstract int getLimit();

    public abstract void setCount(int paramInt);

    public abstract int getCount();
}
