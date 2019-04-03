package nh321.rest;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import nh321.api.DataCleanUtil;

@Path("/api")
public class Cleaner {
    private static final Logger logger = LoggerFactory.getLogger(Cleaner.class);

    private final DataCleanUtil dataCleanUtil;
    @ConfluenceImport
    private final PermissionManager permissionManager;
    @ConfluenceImport
    private final PageManager pageManager;
    @ConfluenceImport
    private final SpaceManager spaceManager;
    private final static int MAX_LIMIT_FOR_VERSIONS = 1000;
    private final static int MAX_LIMIT_FOR_TRASH = 100;
    private final static long NOT_DELETED = 0;
    private final static String ITEM_TYPE_FOR_SPACE_TRASH = "SpaceTrash";
    private final static String END_DAYS_FOR_SPACE_TRASH = "-";

    @Inject
    public Cleaner(DataCleanUtil dataCleanUtil, PermissionManager permissionManager, PageManager pageManager, SpaceManager spaceManager) {
        this.dataCleanUtil = dataCleanUtil;
        this.permissionManager = permissionManager;
        this.pageManager = pageManager;
        this.spaceManager = spaceManager;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/versions/all")
    @AnonymousAllowed
    public Response getAllVersions(@QueryParam("type") String type, @QueryParam("endDays") String endDaysStr, @QueryParam("limit") String limitStr) {
        // Authentication
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.isConfluenceAdministrator(loggedInAppUser)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "Confluence Administrator Permission required!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(type, endDaysStr, limitStr, MAX_LIMIT_FOR_VERSIONS);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        List<SpaceVersionsModel> spaceModels = this.dataCleanUtil.getAllVersionSummary(baseParam.getEndDays(), baseParam.getType());

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                NOT_DELETED,
                NOT_DELETED,
                spaceModels);
        return Response.ok(responseModel).build();
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/versions/all")
    @AnonymousAllowed
    public Response deleteAllVersions(@QueryParam("type") String type, @QueryParam("endDays") String endDaysStr, @QueryParam("limit") String limitStr) {
        // Authentication
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.isConfluenceAdministrator(loggedInAppUser)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "Confluence Administrator Permission required!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(type, endDaysStr, limitStr, MAX_LIMIT_FOR_VERSIONS);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        logger.info(MessageFormat.format("User({0}) deleting versions for all spaces..", new Object[] {loggedInAppUser.getName() }));
        long deleted = this.dataCleanUtil.removeAllVersions(baseParam.getEndDays(), baseParam.getType());

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                this.dataCleanUtil.getCount(),
                deleted,
                new MessageModel("Deleted Versions Count - " + deleted));
        return Response.ok(responseModel).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/versions/space/{spaceKey}")
    @AnonymousAllowed
    public Response getSpaceVersionsBySpaceKey(@QueryParam("type") String type, @QueryParam("endDays") String endDaysStr, @QueryParam("limit") String limitStr, @PathParam("spaceKey") String spaceKey) {
        // Authentication
        if (StringUtils.isEmpty(spaceKey)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorModel("400 Bad Request", "Required fields are missing. : spaceKey")).build();
        }
        Space space = this.spaceManager.getSpace(spaceKey);
        if (space == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorModel("404 Not Found", "Space is not exists!")).build();
        }
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.hasPermission(loggedInAppUser, Permission.VIEW, space)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "View Permission denied!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(type, endDaysStr, limitStr, MAX_LIMIT_FOR_VERSIONS);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        SpaceVersionsModel spaceVersionModel = this.dataCleanUtil.getSpaceVersionSummary(space, baseParam.getEndDays(), baseParam.getType());

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                NOT_DELETED,
                NOT_DELETED,
                spaceVersionModel);
        return Response.ok(responseModel).build();
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/versions/space/{spaceKey}")
    @AnonymousAllowed
    public Response deleteSpaceVersionsBySpaceKey(@QueryParam("type") String type, @QueryParam("endDays") String endDaysStr, @QueryParam("limit") String limitStr, @PathParam("spaceKey") String spaceKey) {
        // Authentication
        if (StringUtils.isEmpty(spaceKey)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorModel("400 Bad Request", "Required fields are missing. : spaceKey")).build();
        }
        Space space = this.spaceManager.getSpace(spaceKey);
        if (space == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorModel("404 Not Found", "Space is not exists!")).build();
        }
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.hasPermission(loggedInAppUser, Permission.ADMINISTER, space)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "Admin Permission denied!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(type, endDaysStr, limitStr, MAX_LIMIT_FOR_VERSIONS);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        logger.info(MessageFormat.format("User({0}) deleting versions for spaceKey {1}", new Object[] {loggedInAppUser.getName(), Long.valueOf(spaceKey) }));
        long deleted = this.dataCleanUtil.removeSpaceVersions(space, baseParam.getEndDays(), baseParam.getType());

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                this.dataCleanUtil.getCount(),
                deleted,
                new MessageModel("Deleted Versions Count - " + deleted));
        return Response.ok(responseModel).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/versions/page/{pageId}")
    @AnonymousAllowed
    public Response getPageVersionsByPageId(@QueryParam("type") String type, @QueryParam("endDays") String endDaysStr, @QueryParam("limit") String limitStr, @PathParam("pageId") long pageId) {
        // Authentication
        if (pageId <= 0L) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorModel("400 Bad Request", "Required fields are missing. : spaceKey")).build();
        }
        Page page = this.pageManager.getPage(pageId);
        if (page == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorModel("404 Not Found", "Space is not exists!")).build();
        }
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.hasPermission(loggedInAppUser, Permission.VIEW, page)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "View Permission denied!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(type, endDaysStr, limitStr, MAX_LIMIT_FOR_VERSIONS);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        PageVersionsModel pageVersionsModel = this.dataCleanUtil.getPageVersionSummary(page, baseParam.getEndDays(), baseParam.getType());

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                NOT_DELETED,
                NOT_DELETED,
                pageVersionsModel);
        return Response.ok(responseModel).build();
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/versions/page/{pageId}")
    @AnonymousAllowed
    public Response deletePageVersionsByPageId(@QueryParam("type") String type, @QueryParam("endDays") String endDaysStr, @QueryParam("limit") String limitStr, @PathParam("pageId") long pageId) {
        // Authentication
        if (pageId <= 0L) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorModel("400 Bad Request", "Required fields are missing. : spaceKey")).build();
        }
        Page page = this.pageManager.getPage(pageId);
        if (page == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorModel("404 Not Found", "Space is not exists!")).build();
        }
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.hasPermission(loggedInAppUser, Permission.ADMINISTER, page)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "Admin Permission denied!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(type, endDaysStr, limitStr, MAX_LIMIT_FOR_VERSIONS);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        logger.info(MessageFormat.format("User({0}) deleting page versions for pageId {1}", new Object[] {loggedInAppUser.getName(), Long.valueOf(pageId) }));
        long deleted = this.dataCleanUtil.removePageVersions(page, baseParam.getEndDays(), baseParam.getType());

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                this.dataCleanUtil.getCount(),
                deleted,
                new MessageModel("Deleted Versions Count - " + deleted));
        return Response.ok(responseModel).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/trash/all")
    @AnonymousAllowed
    public Response getAllGarbages(@QueryParam("limit") String limitStr) {
        // Authentication
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.isConfluenceAdministrator(loggedInAppUser)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "Confluence Administrator Permission required!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(ITEM_TYPE_FOR_SPACE_TRASH, END_DAYS_FOR_SPACE_TRASH, limitStr, MAX_LIMIT_FOR_TRASH);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        List<SpaceTrashModel> trashModels = this.dataCleanUtil.getAllTrashSummary();

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                NOT_DELETED,
                NOT_DELETED,
                trashModels);
        return Response.ok(responseModel).build();
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/trash/all")
    @AnonymousAllowed
    public Response deleteAllGarbages(@QueryParam("limit") String limitStr) {
        // Authentication
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.isConfluenceAdministrator(loggedInAppUser)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "Confluence Administrator Permission required!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(ITEM_TYPE_FOR_SPACE_TRASH, END_DAYS_FOR_SPACE_TRASH, limitStr, MAX_LIMIT_FOR_TRASH);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        logger.info(MessageFormat.format("User({0}) deleting space trash for all", new Object[] {loggedInAppUser.getName() }));
        long deleted = this.dataCleanUtil.removeAllTrash();

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                this.dataCleanUtil.getCount(),
                deleted,
                new MessageModel("Deleted Trash Items Count - " + deleted));
        return Response.ok(responseModel).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/trash/space/{spaceKey}")
    @AnonymousAllowed
    public Response getSpaceGarbagesBySpaceKey(@QueryParam("limit") String limitStr, @PathParam("spaceKey") String spaceKey) {
        // Authentication
        if (StringUtils.isEmpty(spaceKey)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorModel("400 Bad Request", "Required fields are missing. : spaceKey")).build();
        }
        Space space = this.spaceManager.getSpace(spaceKey);
        if (space == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorModel("404 Not Found", "Space is not exists!")).build();
        }
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.hasPermission(loggedInAppUser, Permission.VIEW, space)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "View Permission denied!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(ITEM_TYPE_FOR_SPACE_TRASH, END_DAYS_FOR_SPACE_TRASH, limitStr, MAX_LIMIT_FOR_TRASH);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        SpaceTrashModel spaceTrashModel = this.dataCleanUtil.getSpaceTrashSummary(space);

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                NOT_DELETED,
                NOT_DELETED,
                spaceTrashModel);
        return Response.ok(responseModel).build();
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/trash/space/{spaceKey}")
    @AnonymousAllowed
    public Response deleteSpaceGarbagesBySpaceKey(@QueryParam("limit") String limitStr, @PathParam("spaceKey") String spaceKey) {
        // Authentication
        if (StringUtils.isEmpty(spaceKey)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorModel("400 Bad Request", "Required fields are missing. : spaceKey")).build();
        }
        Space space = this.spaceManager.getSpace(spaceKey);
        if (space == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorModel("404 Not Found", "Space is not exists!")).build();
        }
        ConfluenceUser loggedInAppUser = AuthenticatedUserThreadLocal.get();
        if (!this.permissionManager.hasPermission(loggedInAppUser, Permission.ADMINISTER, space)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorModel("401 Unauthorized", "Admin Permission denied!")).build();
        }

        // Validate Params
        BaseParam baseParam = new BaseParam(ITEM_TYPE_FOR_SPACE_TRASH, END_DAYS_FOR_SPACE_TRASH, limitStr, MAX_LIMIT_FOR_TRASH);
        if (baseParam.getErrorModel().getMessages().size() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(baseParam.getErrorModel()).build();
        }

        // Set up
        this.dataCleanUtil.setLimit(baseParam.getLimit());
        this.dataCleanUtil.setCount(0);

        // Main process
        logger.info(MessageFormat.format("User({0}) deleting space trash for spaceKey {1}", new Object[] {loggedInAppUser.getName(), Long.valueOf(spaceKey) }));
        int deleted = 0;
        deleted = this.dataCleanUtil.removeSpaceTrash(space);

        ResponseModel responseModel = new ResponseModel(
                baseParam.getType(),
                baseParam.getEndDays(),
                baseParam.getLimit(),
                this.dataCleanUtil.getCount(),
                this.dataCleanUtil.getCount(),
                deleted,
                new MessageModel("Deleted Trash Items Count - " + deleted));
        return Response.ok(responseModel).build();
    }

}