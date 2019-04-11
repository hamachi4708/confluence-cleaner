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
    public List<Attachment> getAttachmentVersions(Attachment attachment, int endDays) {
        List<Attachment> resultAttachments = new ArrayList<Attachment>();
        Date lastUpdatedOrCreatedDate = getCreatedOrUpdatedDate(endDays);
        List<Attachment> vAttachments = this.attachmentManager.getPreviousVersions(attachment);

        for (Attachment vAttachment : vAttachments) {
            if (vAttachment.getLastModificationDate().before(lastUpdatedOrCreatedDate)) {
                resultAttachments.add(vAttachment);
          }
        }

        return resultAttachments;
    }

    @Override
    public List<AbstractPage> getPageVersions(Page page, int endDays, String type) {
        List<AbstractPage> resultVersions = new ArrayList<AbstractPage>();
        Date lastUpdatedOrCreatedDate = getCreatedOrUpdatedDate(endDays);
        List<VersionHistorySummary> versions = this.pageManager.getVersionHistorySummaries(page);

        for (VersionHistorySummary vSummary : versions) {
            if (page.getId() != vSummary.getId() && vSummary.getLastModificationDate().before(lastUpdatedOrCreatedDate)) {
                AbstractPage vPage = this.pageManager.getPage(vSummary.getId());
                if (vPage == null) {
                    vPage = this.pageManager.getBlogPost(vSummary.getId());

                }
                if (vPage != null) {
                    resultVersions.add(vPage);
                }
            }
        }

        return resultVersions;
    }

    @Override
    public long removePageVersions(Page page, int endDays, String type) {
        long delete_index = 0;
        if (this.count >= this.limit) {
            return delete_index;
        }

        if ("all".equals(type) || "page".equals(type)) {
            List<AbstractPage> aPages = getPageVersions(page, endDays, type);

            for (AbstractPage aPage : aPages) {
                this.pageManager.removeHistoricalVersion(aPage);
                delete_index++;
            }

            if (delete_index > 0) {
                this.count++;
                if (this.count >= this.limit) {
                    return delete_index;
                }
            }
        }

        if ("all".equals(type) || "attachment".equals(type)) {
            List<Attachment> attachments = attachmentManager.getLatestVersionsOfAttachments(page);

            for (Attachment attachment : attachments) {
                List<Attachment> targetAttachments = getAttachmentVersions(attachment, endDays);

                for (Attachment targetAttachment : targetAttachments) {
                    this.attachmentManager.removeAttachmentVersionFromServer(targetAttachment);
                    delete_index++;
                }

                if (targetAttachments.size() > 0) {
                    this.count++;
                    if (this.count >= this.limit) {
                        return delete_index;
                    }
                }
            }
        }

        return delete_index;
    }

    @Override
    public long removeSpaceVersions(Space space, int endDays, String type) {
        Date lastUpdatedOrCreatedDate = getCreatedOrUpdatedDate(endDays);
        Collection<Page> pages = this.pageManager.getPages(space, true);
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
    public PageVersionsModel getPageVersionSummary(Page page, int endDays, String type) {
        List<AbstractPage> resultVersions = new ArrayList<AbstractPage>();
        if ("all".equals(type) || "page".equals(type)) {
            resultVersions = getPageVersions(page, endDays, type);
        }
        PageVersionsModel pageModel = new PageVersionsModel();
        String modifier = page.getLastModifier() != null ? page.getLastModifier().getName() : "";
        pageModel.setId(page.getId());
        pageModel.setTitle(page.getTitle());
        pageModel.setVersionCount(resultVersions.size());
        pageModel.setLastModifier(modifier);
        pageModel.setLastModified(page.getLastModificationDate());

        long totalPageVersionCount = pageModel.getVersionCount();
        if (pageModel.getVersionCount() > 0) {
            this.count++;
            if (this.count >= this.limit) {
                pageModel.setTotalVersionCount(totalPageVersionCount);
                return pageModel;

            }
        }

        if ("all".equals(type) || "attachment".equals(type)) {
            List<Attachment> attachments = attachmentManager.getLatestVersionsOfAttachments(page);
            List<Attachment> attachmentVersions = new ArrayList<Attachment>();
            List<AttachmentModel> attachmentModels = new ArrayList<AttachmentModel>();

            for (Attachment attachment : attachments) {
                List<Attachment> tmpAttachmentVersions = getAttachmentVersions(attachment, endDays);
                int tmpAttachmentVersionsCount = tmpAttachmentVersions.size();

                if (tmpAttachmentVersionsCount > 0) {
                    attachmentVersions.addAll(tmpAttachmentVersions);
                    totalPageVersionCount += tmpAttachmentVersions.size();
                    String last_modifier = attachment.getLastModifier() != null ? attachment.getLastModifier().getName() : "";
                    AttachmentModel model = new AttachmentModel(
                            attachment.getId(),
                            attachment.getTitle(),
                            tmpAttachmentVersionsCount,
                            last_modifier,
                            attachment.getLastModificationDate()
                            );
                    attachmentModels.add(model);
                    this.count++;
                    if (this.count >= this.limit) {
                        break;

                    }
                }
            }
            pageModel.setAttachments(attachmentModels);

        }

        pageModel.setTotalVersionCount(totalPageVersionCount);
        return pageModel;
    }

    @Override
    public SpaceVersionsModel getSpaceVersionSummary(Space space, int endDays, String type) {
        Collection<Page> pages = this.pageManager.getPages(space, true);
        List<PageVersionsModel> pageModels = new ArrayList<PageVersionsModel>();

        long totalSpaceVersionCount = 0;
        for (Page page : pages) {
            PageVersionsModel pageModel = getPageVersionSummary(page, endDays, type);

            if (pageModel.getTotalVersionCount() > 0) {
                totalSpaceVersionCount += pageModel.getTotalVersionCount();
                pageModels.add(pageModel);
            }

            if (this.count >= this.limit) {
                break;
            }
        }
        SpaceVersionsModel spaceModel = new SpaceVersionsModel(space.getKey(), space.getDisplayTitle(), totalSpaceVersionCount, pageModels);

        return spaceModel;
    }

    @Override
    public List<SpaceVersionsModel> getAllVersionSummary(int endDays, String type) {
        List<SpaceVersionsModel> spaceModels = new ArrayList<SpaceVersionsModel>();
        for (Space space : this.spaceManager.getAllSpaces()) {
            SpaceVersionsModel spaceModel = this.getSpaceVersionSummary(space, endDays, type);

            if(spaceModel.getTotalVersionCount() > 0) {
                spaceModels.add(spaceModel);
            }

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

            if (trashModel.getNumberOfItemsInTrash() > 0) {

                trashModels.add(trashModel);

                this.count++;
                if (this.count >= this.limit) {
                    return trashModels;
                }
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
