package ut.nh321.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.confluence.core.VersionHistorySummary;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.TrashManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;

import nh321.api.DataCleanUtil;
import nh321.api.DateTimeUtil;
import nh321.impl.DataCleanUtilImpl;


@RunWith(Enclosed.class)
public class DataCleanUtilTest {

    @RunWith (MockitoJUnitRunner.class)
    public static class testGetCreatedOrUpdatedDate {
        @Mock
        private PageManager pageManager;
        @Mock
        private AttachmentManager attachmentManager;
        @Mock
        private TrashManager trashManager;
        @Mock
        private SpaceManager spaceManager;
        @Mock
        private DateTimeUtil dateTimeUtil;
        private DataCleanUtil dataCleanUtil;

        @Before
        public void setup() {
            Calendar c = Calendar.getInstance();
            c.set(2015, 3, 5); // 2015/04/05
            Date date = c.getTime();
            when(dateTimeUtil.getDate()).thenReturn(date);
            this.dataCleanUtil = new DataCleanUtilImpl(pageManager, attachmentManager, trashManager, spaceManager, dateTimeUtil);
        }

        @Test
        public void oneIsYesterday() {
            Date d = dataCleanUtil.getCreatedOrUpdatedDate(1);
            assertEquals("DateError!", "2015/04/04", new SimpleDateFormat("yyyy/MM/dd").format(d));
        }

        @Test
        public void zeroIsToday() {
            Date d = dataCleanUtil.getCreatedOrUpdatedDate(0);
            assertEquals("DateError!", "2015/04/05", new SimpleDateFormat("yyyy/MM/dd").format(d));
        }

        @Test
        public void minusIsToday() {
            Date d = dataCleanUtil.getCreatedOrUpdatedDate(-1);
            assertEquals("DateError!", "2015/04/05", new SimpleDateFormat("yyyy/MM/dd").format(d));
        }
    }

    @RunWith (MockitoJUnitRunner.class)
    public static class removeAttachmentVersions {
        @Mock
        private PageManager pageManager;
        @Mock
        private AttachmentManager attachmentManager;
        @Mock
        private TrashManager trashManager;
        @Mock
        private SpaceManager spaceManager;
        @Mock
        private DateTimeUtil dateTimeUtil;
        private DataCleanUtil dataCleanUtil;
        private Attachment attachmentMain;

        @Before
        public void setup() {
            // Base date
            Calendar c = Calendar.getInstance();
            c.set(2015, 3, 5); // 2015/04/05
            Date date = c.getTime();
            when(dateTimeUtil.getDate()).thenReturn(date);

            List<Attachment> vAttachments = new ArrayList<Attachment>();
            this.attachmentMain = new Attachment();
            Attachment attachmentAfter = new Attachment();
            Attachment attachmentBefore = new Attachment();

            // after
            Calendar cA = Calendar.getInstance();
            cA.set(2015, 3, 5);
            Date dateA = cA.getTime();
            attachmentAfter.setId(20150405L);
            attachmentAfter.setLastModificationDate(dateA);
            vAttachments.add(attachmentAfter);
            doNothing().when(attachmentManager).removeAttachmentVersionFromServer(attachmentAfter);

            // before(delete target)
            Calendar cB = Calendar.getInstance();
            cB.set(2015, 3, 1);
            Date dateB = cB.getTime();
            attachmentBefore.setId(20150401L);
            attachmentBefore.setLastModificationDate(dateB);
            vAttachments.add(attachmentBefore);
            doNothing().when(attachmentManager).removeAttachmentVersionFromServer(attachmentBefore);

            when(attachmentManager.getPreviousVersions(attachmentMain)).thenReturn(vAttachments);

            this.dataCleanUtil = new DataCleanUtilImpl(pageManager, attachmentManager, trashManager, spaceManager, dateTimeUtil);
        }

        @Test
        public void main() {
            int endDays = 2;
            assertEquals("DeleteError!!", 1L, dataCleanUtil.removeAttachmentVersions(attachmentMain, endDays));
        }
    }

    @RunWith (MockitoJUnitRunner.class)
    public static class removePageVersions {
        @Mock
        private PageManager pageManager;
        @Mock
        private AttachmentManager attachmentManager;
        @Mock
        private TrashManager trashManager;
        @Mock
        private SpaceManager spaceManager;
        @Mock
        private DateTimeUtil dateTimeUtil;
        private DataCleanUtil dataCleanUtil;
        private Page pageMain;
        private Attachment attachmentMain;

        @Before
        public void setup() {
            // Base date
            Calendar c = Calendar.getInstance();
            c.set(2015, 3, 5); // 2015/04/05
            Date date = c.getTime();
            when(dateTimeUtil.getDate()).thenReturn(date);

            // for Page ------------------------------------------------------------------------------------------
            List<VersionHistorySummary> versions = new ArrayList<VersionHistorySummary>();
            pageMain = new Page();
            pageMain.setId(1L);
            VersionHistorySummary.Builder vBuilder = new VersionHistorySummary.Builder();

            // after
            Calendar cAfter = Calendar.getInstance();
            cAfter.set(2015, 3, 5);
            Date dateAfter = cAfter.getTime();
            VersionHistorySummary versionAfter = vBuilder.withId(20150405L).withLastModificationDate(dateAfter).build();
            versions.add(versionAfter);

            // before(delete target)
            Calendar cBefore = Calendar.getInstance();
            cBefore.set(2015, 3, 1);
            Date dateBefore = cBefore.getTime();
            VersionHistorySummary verionBefore = vBuilder.withId(20150401L).withLastModificationDate(dateBefore).build();
            versions.add(verionBefore);
            Page vPage = new Page();
            when(pageManager.getPage(verionBefore.getId())).thenReturn(vPage);
            doNothing().when(pageManager).removeHistoricalVersion(vPage);

            // sameId
            VersionHistorySummary verionSameId = vBuilder.withId(1L).build();
            versions.add(verionSameId);

            // blog(delete target)
            VersionHistorySummary verionBlog = vBuilder.withId(2L).withLastModificationDate(dateBefore).build();
            versions.add(verionBlog);
            when(pageManager.getPage(verionBlog.getId())).thenReturn(null);
            BlogPost vBlog = new BlogPost();
            when(pageManager.getBlogPost(verionBlog.getId())).thenReturn(vBlog);
            doNothing().when(pageManager).removeHistoricalVersion(vBlog);

            // not blog
            VersionHistorySummary verionNotBlog = vBuilder.withId(3L).withLastModificationDate(dateBefore).build();
            versions.add(verionNotBlog);
            when(pageManager.getPage(verionNotBlog.getId())).thenReturn(null);
            when(pageManager.getBlogPost(verionNotBlog.getId())).thenReturn(null);

            when(pageManager.getVersionHistorySummaries(pageMain)).thenReturn(versions);

            // for Attachment ------------------------------------------------------------------------------------
            List<Attachment> vAttachments = new ArrayList<Attachment>();
            this.attachmentMain = new Attachment();
            Attachment attachmentAfter = new Attachment();
            Attachment attachmentBefore = new Attachment();

            // after
            Calendar cA = Calendar.getInstance();
            cA.set(2015, 3, 5);
            Date dateA = cA.getTime();
            attachmentAfter.setId(20150405L);
            attachmentAfter.setLastModificationDate(dateA);
            vAttachments.add(attachmentAfter);
            doNothing().when(attachmentManager).removeAttachmentVersionFromServer(attachmentAfter);

            // before(delete target)
            Calendar cB = Calendar.getInstance();
            cB.set(2015, 3, 1);
            Date dateB = cB.getTime();
            attachmentBefore.setId(20150401L);
            attachmentBefore.setLastModificationDate(dateB);
            vAttachments.add(attachmentBefore);
            doNothing().when(attachmentManager).removeAttachmentVersionFromServer(attachmentBefore);

            when(attachmentManager.getPreviousVersions(attachmentMain)).thenReturn(vAttachments);

            // for All --------------------------------------------------------------------------------------------
            this.dataCleanUtil = new DataCleanUtilImpl(pageManager, attachmentManager, trashManager, spaceManager, dateTimeUtil);
        }

        @Test
        public void main() {
            int endDays = 2;
            String type = "page";
            assertEquals("DeleteError!!", 2L, dataCleanUtil.removePageVersions(pageMain, endDays, type));
        }

        @Test
        public void limit() {
            int endDays = 2;
            String type = "all";
            this.dataCleanUtil.setLimit(1);
            assertEquals("DeleteError!!", 2L, dataCleanUtil.removePageVersions(pageMain, endDays, type));
            assertEquals("DeleteError!!", 1L, this.dataCleanUtil.getCount());
        }

        @Test
        public void over() {
            int endDays = 2;
            String type = "all";
            this.dataCleanUtil.setLimit(1);
            this.dataCleanUtil.setCount(1);
            assertEquals("DeleteError!!", 0L, dataCleanUtil.removePageVersions(pageMain, endDays, type));
        }
    }

    @RunWith (MockitoJUnitRunner.class)
    public static class removeSpaceTrash {
        @Mock
        private PageManager pageManager;
        @Mock
        private AttachmentManager attachmentManager;
        @Mock
        private TrashManager trashManager;
        @Mock
        private SpaceManager spaceManager;
        @Mock
        private DateTimeUtil dateTimeUtil;
        private DataCleanUtil dataCleanUtil;
        private Space space;

        @Before
        public void setup() {
            Calendar c = Calendar.getInstance();
            c.set(2015, 3, 5); // 2015/04/05
            Date date = c.getTime();
            when(dateTimeUtil.getDate()).thenReturn(date);

            space = new Space();
            when(trashManager.getNumberOfItemsInTrash(space)).thenReturn(100);
            doNothing().when(trashManager).emptyTrash(space);

            this.dataCleanUtil = new DataCleanUtilImpl(pageManager, attachmentManager, trashManager, spaceManager, dateTimeUtil);
        }

        @Test
        public void main() {
            assertEquals("DeleteError!!", 100, dataCleanUtil.removeSpaceTrash(space));
        }
    }

    @RunWith (MockitoJUnitRunner.class)
    public static class removeAllTrash {
        @Mock
        private PageManager pageManager;
        @Mock
        private AttachmentManager attachmentManager;
        @Mock
        private TrashManager trashManager;
        @Mock
        private SpaceManager spaceManager;
        @Mock
        private DateTimeUtil dateTimeUtil;
        private DataCleanUtil dataCleanUtil;

        @Before
        public void setup() {
            Calendar c = Calendar.getInstance();
            c.set(2015, 3, 5); // 2015/04/05
            Date date = c.getTime();
            when(dateTimeUtil.getDate()).thenReturn(date);

            List<Space> spaces = new ArrayList<Space>();

            Space spaceA = new Space();
            spaceA.setId(1L);
            when(trashManager.getNumberOfItemsInTrash(spaceA)).thenReturn(60);
            doNothing().when(trashManager).emptyTrash(spaceA);
            spaces.add(spaceA);

            Space spaceB = new Space();
            spaceB.setId(2L);
            when(trashManager.getNumberOfItemsInTrash(spaceB)).thenReturn(50);
            doNothing().when(trashManager).emptyTrash(spaceB);
            spaces.add(spaceB);

            Space spaceC = new Space();
            spaceC.setId(3L);
            when(trashManager.getNumberOfItemsInTrash(spaceC)).thenReturn(40);
            doNothing().when(trashManager).emptyTrash(spaceC);
            spaces.add(spaceC);

            when(spaceManager.getAllSpaces()).thenReturn(spaces);
            this.dataCleanUtil = new DataCleanUtilImpl(pageManager, attachmentManager, trashManager, spaceManager, dateTimeUtil);
        }

        @Test
        public void main() {
            dataCleanUtil.setLimit(100);
            assertEquals("DeleteError!!", 150L, dataCleanUtil.removeAllTrash());
        }

        @Test
        public void limit() {
            dataCleanUtil.setLimit(2);
            assertEquals("DeleteError!!", 110L, dataCleanUtil.removeAllTrash());
        }
    }
}