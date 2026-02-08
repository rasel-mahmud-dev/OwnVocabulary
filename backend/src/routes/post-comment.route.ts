import { Router } from "express";
const router = Router();

import postCommentController from "../controllers/post-comment.controller";
import { checkApiKeys } from "../middlewares";

router.get('/api/v2/post-comment', postCommentController.getPostComments);
router.put('/api/v2/post-comment/:uid', checkApiKeys, postCommentController.updatePostComment);
router.get('/api/v2/post-comment/pull', checkApiKeys, postCommentController.postCommentPull);

export default router
