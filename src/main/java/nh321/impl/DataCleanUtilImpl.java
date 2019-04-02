package nh321.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.VersionHistorySummary;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.TrashManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import nh321.api.DataCleanUtil;
import nh321.api.DateTimeUtil;
import nh321.rest.AttachmentModel;
import nh321.rest.PageVersionsModel;
import nh321.rest.SpaceTrashModel;
import nh321.rest.SpaceVersionsModel;

@ExportAsService
@Named
public class DataCleanUtilImpl implements DataCleanUtil {
    private static final Logger logger = LoggerFactory.getLogger(DataCleanUtil.class);

    @ComponentImport
    private final PageManager pageManager;
    @ComponentImport
    private final AttachmentManager attachmentManager;
    @ComponentImport
    private final TrashManager trashManager;
    @ComponentImport
    private final SpaceManager spaceManager;
    private DateTimeUtil dateTimeUtil;
    private int limit;
    private int count;

    @Inject
    public DataCleanUtilImpl(PageManager pageManager, AttachmentManager attachmentManager, TrashManager trashManager, SpaceManager spaceManager, DateTimeUtil dateTimeUtil) {
        this.pageManager = pageManager;
        this.attachmentManager = attachmentManager;
        this.trashManager = trashManager;
        this.spaceManager = spaceManager;
        this.limit = 1000;
        this.count = 0;
        this.dateTimeUtil = dateTimeUtil;
    }

    @Override
    public Date getCreatedOrUpdatedDate(int endDays) {
        Date defaultDate = dateTimeUtil.getDate();
        try {
            int lastCreatedOrUpdated = Integer.parseInt("-" + endDays);
            Calendar cal = Calendar.getInstance();
            cal.setTime(defaultDate);
            cal.add(Calendar.DAY_OF_MONTH, lastCreatedOrUpdated);
            return cal.getTime();

        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);

        }

        return defaultDate;
    }

    @Override
    public long removeAttachmentVersions(Attachment attachment, int endDays) {
        Date lastUpdatedOrCreatedDate = getCreatedOrUpdatedDate(endDays);
        List<Attachment> vAttachments = this.attachmentManager.getPreviousVersions(attachment);
        long delete_index = 0;

        for (Attachment vAttachment : vAttachments) {
            if (vAttachment.getLastModificationDate().before(lastUpdatedOrCreatedDate)) {
                this.attachmentManager.removeAttachmentVersionFromServer(vAttachment);
                delete_index++;
          }
        }

        return delete_index;
    }

    @Override
    public long removePageVersions(AbstractPage page, int endDays, String type) {
        long delete_index = 0;
        if (this.count >= this.limit) {
            return delete_index;
        }
        Date lastUpdatedOrCreatedDate = getCreatedOrUpdatedDate(endDays);

        if ("all".equals(type) || "page".equals(type)) {
            List<VersionHistorySummary> versions = this.pageManager.getVersionHistorySummaries(page);

            for (VersionHistorySummary vSummary : versions) {
                if (page.getId() != vSummary.getId() && vSummary.getLastModificationDate().before(lastUpdatedOrCreatedDate)) {
                    AbstractPage vPage = this.pageManager.getPage(vSummary.getId());
                    if (vPage == null) {
                        vPage = this.pageManager.getBlogPost(vSummary.getId());

                    }
                    if (vPage != null) {
                        this.pageManager.removeHistoricalVersion(vPage);
                        delete_index++;
                    }
                }
            }

            this.count++;
            if (this.count >= this.limit) {
                return delete_index;
            }
        }

        if ("all".equals(type) || "attachment".equals(type)) {
            List<Attachment> attachments = attachmentManager.getLatestVersionsOfAttachments(page);

            for (Attachment attachment : attachments) {
                delete_index += removeAttachmentVersions(attachment, endDays);

                this.count++;
                if (this.count >= this.limit) {
                    return delete_index;
                }
            }
        }

        return delete_index;
    }

    @Override
    public long removeSpaceVersions(Space space, int endDays, String type) {
        Date lastUpdatedOrCreatedDate = getCreatedOrUpdatedDate(endDays);
        @SuppressWarnings("unchecked")
        Collection<Page> pages = this.pageManager.getPermissionPages(space);
        long delete_index = 0L;

        for (Page page : pages) {
            if (page.getCreationDate().before(lastUpdatedOrCreatedDate)) {
                delete_index += removePageVersions(page, endDays, type);

                if (this.count >= this.limit) {
                    return delete_index;
                }
            }
        }

        return delete_index;
    }

    @Override
    public long removeAllVersions(int endDays, String type) {
        long delete_index = 0L;
        for (Space space : this.spaceManager.getAllSpaces()) {
            delete_index += this.removeSpaceVersions(space, endDays, type);

            if (this.count >= this.limit) {
                return delete_index;
            }
        }
        return delete_index;
    }

    @Override
    public int removeSpaceTrash(Space space) {
        int numberOfItems = this.trashManager.getNumberOfItemsInTrash(space);
        this.trashManager.emptyTrash(space);

        return numberOfItems;
    }

    @Override
    public long removeAllTrash() {
        long deleted = 0L;
        for (Space space : this.spaceManager.getAllSpaces()) {
            deleted += this.removeSpaceTrash(space);

            this.count++;
            if (this.count >= this.limit) {
                return deleted;
            }
        }
        return deleted;
    }

    @Override
    public AttachmentModel getAttachmentVersionSummary(Attachment attachment, int endDays) {
        Date lastUpdatedOrCreatedDate = getCreatedOrUpdatedDate(endDays);
        List<Attachment> vAttachments = this.attachmentManager.getPreviousVersions(attachment);
        int versionCount = 0;

        for (Attachment vAttachment : vAttachments) {
            if (vAttachment.getLastModificationDate().before(lastUpdatedOrCreatedDate)) {
                versionCount++;
          }
        }

        String last_modifier = attachment.getLastModifier() != null ? attachment.getLastModifier().getName() : "";
        AttachmentModel model = new AttachmentModel(
                attachment.getId(),
                attachment.getTitle(),
                versionCount,
                last_modifier,
                attachment.getLastModificationDate()
                );

        return model;
    }

    @Override
    public PageVersionsModel getPageVersionSummary(Page page, int endDays, String type) {
        int versionCount = 0;
        Date lastUpdatedOrCreatedDate = getCreatedOrUpdatedDate(endDays);
        List<VersionHistorySummary> versions = this.pageManager.getVersionHistorySummaries(page);

        for (VersionHistorySummary vSummary : versions) {
            if (page.getId() != vSummary.getId() && vSummary.getLastModificationDate().before(lastUpdatedOrCreatedDate)) {
                AbstractPage vPage = this.pageManager.getPage(vSummary.getId());
                if (vPage == null) {
                    vPage = this.pageManager.getBlogPost(vSummary.getId());

                }
                if (vPage != null) {
                    versionCount++;
                }
            }
        }

        PageVersionsModel pageModel = new PageVersionsModel();
        String modifier = page.getLastModifier() != null ? page.getLastModifier().getName() : "";
        pageModel.setId(page.getId());
        pageModel.setTitle(page.getTitle());
        pageModel.setVersionCount(versionCount);
        pageModel.setLastModifier(modifier);
        pageModel.setLastModified(page.getLastModificationDate());

        long totalVersionCount = pageModel.getVersionCount();
        if (pageModel.getVersionCount() > 0) {
            this.count++;
            if (this.count >= this.limit) {
                pageModel.setTotalVersionCount(totalVersionCount);
                return pageModel;

            }
        }

        if ("all".equals(type) || "attachment".equals(type)) {
            List<Attachment> attachments = attachmentManager.getLatestVersionsOfAttachments(page);
            List<AttachmentModel> attachmentModels = new ArrayList<AttachmentModel>();

            for (Attachment attachment : attachments) {
                AttachmentModel attachmentModel = getAttachmentVersionSummary(attachment, endDays);

                if (attachmentModel.getVersionCount() > 0) {
                    attachmentModels.add(attachmentModel);
                    totalVersionCount += attachmentModel.getVersionCount();

                    this.count++;
                    if (this.count >= this.limit) {
                        break;

                    }
                }
            }
            pageModel.setAttachments(attachmentModels);

        }

        pageModel.setTotalVersionCount(totalVersionCount);

        return pageModel;
    }

    @Override
    public SpaceVersionsModel getSpaceVersionSummary(Space space, int endDays, String type) {
        Collection<Page> pages = this.pageManager.getPages(space, true);
        List<PageVersionsModel> pageModels = new ArrayList<PageVersionsModel>();

        long totalVersionCount = 0;
        for (Page page : pages) {
            PageVersionsModel pageVersionsModel = getPageVersionSummary(page, endDays, type);
            if (pageVersionsModel.getTotalVersionCount() > 0) {
                pageModels.add(pageVersionsModel);
                totalVersionCount += pageVersionsModel.getTotalVersionCount();

                if (this.count >= this.limit) {
                    break;
                }
            }

        }
        SpaceVersionsModel spaceModel = new SpaceVersionsModel(space.getKey(), space.getDisplayTitle(), totalVersionCount, pageModels);

        return spaceModel;
    }

    @Override
    public List<SpaceVersionsModel> getAllVersionSummary(int endDays, String type) {
        List<SpaceVersionsModel> spaceModels = new ArrayList<SpaceVersionsModel>();
        for (Space space : this.spaceManager.getAllSpaces()) {
            SpaceVersionsModel spaceModel = this.getSpaceVersionSummary(space, endDays, type);
            spaceModels.add(spaceModel);

            if (this.count >= this.limit) {
                return spaceModels;
            }
        }
        return spaceModels;
    }

    @Override
    public SpaceTrashModel getSpaceTrashSummary(Space space) {
        int numberOfItems = this.trashManager.getNumberOfItemsInTrash(space);
        SpaceTrashModel trashModel = new SpaceTrashModel(space.getKey(), space.getDisplayTitle(), numberOfItems);

        return trashModel;
    }

    @Override
    public List<SpaceTrashModel> getAllTrashSummary() {
        List<SpaceTrashModel> trashModels = new ArrayList<SpaceTrashModel>();
        for (Space space : this.spaceManager.getAllSpaces()) {
            SpaceTrashModel trashModel = this.getSpaceTrashSummary(space);
            trashModels.add(trashModel);

            this.count++;
            if (this.count >= this.limit) {
                return trashModels;
            }
        }
        return trashModels;
    }

    @Override
    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public int getLimit() {
        return this.limit;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {
        return this.count;
    }

}
