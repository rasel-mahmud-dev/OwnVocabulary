import RepositoryBase from "../respository/repository.base";
import { Request, Response } from "express"
import date from "../utils/date";

class PostCommentController {

    async getPostComments(req: Request, res: Response) {
        const items = await RepositoryBase.findAll("post-comment", {})
        res.status(200).json({
            data: items
        })
    }

    async updatePostComment(req: Request, res: Response) {
        const {
            postId,
            userId,
            text,
            audioUrl,
            mediaUrl,
            mediaType,
            attachments,
            createdAt,
            updatedAt,
            syncStatus,
            isDeleted
        } = req.body

        const uid = req.params.uid

        await RepositoryBase.updateOneWithUpsert("post-comment", {
            uid: uid
        }, {
            postId,
            userId,
            text,
            audioUrl,
            mediaUrl,
            mediaType,
            attachments,
            syncStatus: "synced",
            isDeleted: !!isDeleted,
            updatedAt: Number(updatedAt) || date.now(),
            createdAt: Number(createdAt) || date.now(),
        })

        res.status(200).json({ message: "Successfully updated" })
    }

    async postCommentPull(req: Request, res: Response) {
        const since = Number(req.query?.since) || 0
        const limit = 50
        const query = { updatedAt: { $gt: Number(since) } };
        const items = await RepositoryBase.findAll("post-comment", query, {
            sort: { updatedAt: 1 },
            limit: limit
        })

        res.json({
            data: items,
            hasMore: items?.length > 0
        });
    }
}

export default (new PostCommentController())
