import RepositoryBase from "../respository/repository.base";
import { Request, Response } from "express"
import date from "../utils/date";

class CommentController {

    async getComments(req: Request, res: Response) {
        const items = await RepositoryBase.findAll("comment", {})
        res.status(200).json({
            data: items
        })
    }

    async updateComment(req: Request, res: Response) {
        const {
            username,
            text,
            audioUrl,
            mediaUrl,
            mediaType,
            attachments,
            parentId,
            createdAt,
            updatedAt,
            syncStatus,
            isDeleted
        } = req.body

        const uid = req.params.uid // For comments, this might be _id or a separate uid

        await RepositoryBase.updateOneWithUpsert("comment", {
            _id: uid
        }, {
            username,
            text,
            audioUrl,
            mediaUrl,
            mediaType,
            attachments,
            parentId,
            syncStatus: "synced",
            isDeleted: !!isDeleted,
            updatedAt: Number(updatedAt) || date.now(),
            createdAt: Number(createdAt) || date.now(),
        })

        res.status(200).json({ message: "Successfully updated" })
    }

    async commentPull(req: Request, res: Response) {
        const since = Number(req.query?.since) || 0
        const limit = 50
        const query = { updatedAt: { $gt: Number(since) } };
        const items = await RepositoryBase.findAll("comment", query, {
            sort: { updatedAt: 1 },
            limit: limit
        })

        res.json({
            data: items,
            hasMore: items?.length > 0
        });
    }
}

export default (new CommentController())
