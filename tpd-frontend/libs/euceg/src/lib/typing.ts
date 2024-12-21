import { FilterTerms } from '@devacfr/util';
import { AttachmentRequest, EcigProduct, TobaccoProduct } from '.';
import { Observable } from 'rxjs';

export type ProductType = 'TOBACCO' | 'ECIGARETTE';

export type SubmissionStatus =
    /** Indicates the submission has not yet been sent */
    | 'NOT_SEND'
    /** indicates the submission awaits */
    | 'PENDING'
    /** indicates the submission is sending. */
    | 'SUBMITTING'
    /** indicates the submission is success and all dependents elements MUST be success. */
    | 'SUBMITTED'
    /** indicates the submission failed. */
    | 'ERROR'
    /**
     * indicate the submission has cancelled before sent. Once the submission(SUBMITTING) is been sent, the submission
     * can not be cancelled.
     */
    | 'CANCELLED';

export type ProductStatus =
    /** */
    | 'DRAFT'
    /** */
    | 'IMPORTED'
    /** */
    | 'VALID'
    /** */
    | 'SENT';

export type ProductPirStatus =
    /** */
    | 'AWAITING'
    /** */
    | 'INACTIVE'
    /** */
    | 'ACTIVE'
    /** */
    | 'WITHDRAWN';
export const ProductPirStatusList: ProductPirStatus[] = ['AWAITING', 'INACTIVE', 'ACTIVE', 'WITHDRAWN'];

export type SubmitterStatus =
    /** */
    | 'DRAFT'
    /** */
    | 'IMPORTED'
    /** */
    | 'VALID'
    /** */
    | 'SENT';

export type AttachmentAction =
    /** Create a new attachment. */
    | 'CREATE'
    /** Update an existing attachment. */
    | 'UPDATE';

export type AttachmentSendStatus =
    /** The attachment has not been sent yet. */
    | 'NO_SEND'
    /** The attachment is sending, but not received acknowledge yet. */
    | 'SENDING'
    /** the attachment has been sent and received success. */
    | 'SENT';

export type SendType = 'MANUAL' | 'IMMEDIAT';

export enum PayloadType {
    /** */
    SUBMITER_DETAILS,
    /** */
    ATTACHMENT,
    /** */
    SUBMISSION,
    /** */
    ERROR_RESPONSE,
}

export type PayloadTypeString = keyof typeof PayloadType;

export enum TransmitStatus {
    /**
     * Indicate the associated payload is in an awaiting send state.
     * <p>
     * all associated element Attachements and Submitter Detail are sent.
     * </p>
     * )
     */
    AWAITING = 'Waiting',
    /**
     * Indicate the associated payload has been sent.
     */
    PENDING = 'Pending', // READY_TO_SEND, SEND_ENQUEUED, SEND_IN_PROGRESS, WAITING_FOR_RECEIPT, ACKNOWLEDGED,
    // ACKNOWLEDGED_WITH_WARNING
    /**
     * Indicate a response has been received.
     */
    RECEIVED = 'Received', // RECEIVED, RECEIVED_WITH_WARNINGS
    /**
     * Indicate that Domibus has rejected the request.
     */
    REJECTED = 'Rejected', // SEND_ATTEMPT_FAILED, SEND_FAILURE, NOT_FOUND
    /**
     * Indicate that Domibus has deleted the response.
     */
    DELETED = 'Deleted', // DELETED
    /**
     * indicate the sent has been cancelled by user.
     */
    CANCELLED = 'Cancelled',
}

export enum SubmissionReportType {
    submission = 'submission',
    tobaccoProduct = 'tobaccoProduct',
    novelTobaccoProduct = 'novelTobaccoProduct',
    ecigaretteProduct = 'ecigaretteProduct',
}

export type TransmitStatusString = keyof typeof TransmitStatus;

export type BulkAction = 'exportExcel' | 'sendSubmission' | 'createSubmission';

export interface BulkRequest {
    action: BulkAction;
    filters?: FilterTerms;
    data?: Record<string, string>;
}

export interface UpdatePirStatusRequest {
    productNumber: string;
    newStatus: ProductPirStatus;
}

export interface ProductUpdateRequest {
    productNumber: string;
    generalComment: string;
    previousProductNumber: string;
    product: TobaccoProduct | EcigProduct;
}

export interface AttachmentUpdate {
    attachmentId: string;
    filename: string;
    confidential: boolean;
    action: AttachmentAction;
    sendStatus: AttachmentSendStatus;
}

export interface ProductRevision {
    id: string;
    version: number;
    productNumber: string;
    status: ProductStatus;
    pirStatus: ProductPirStatus;
    modifiedBy: string;
    modifiedDate: Date;
    createdBy: string;
    createdDate: Date;
}

export interface SubmitterRevision {
    id: number;
    version: number;
    submitterId: string;
    name: string;
    status: SubmitterStatus;
    country: string;
    modifiedBy: string;
    modifiedDate: Date;
}

export interface SubmissionSendRequest {
    productNumber: string;
    submissionType: string;
    sendType: SendType;
}

export interface SubmitterDifference {
    submitterId: string;
    originalRevision: number;
    revisedRevision: number;
    submitterChangeType: 'Unchanged' | 'Added' | 'Modified' | 'Deleted';
    submitterDetailsChangeType: 'Unchanged' | 'Added' | 'Modified' | 'Deleted';
    submitterPatch: string;
    submitterDetailsPatch: string;
}

export interface SubmitterDetail {
    [name: string]: any;
}

export interface SubmitterData {
    [name: string]: any;
}

export interface ProductRevisionDiffItem {
    productNumber: string;
    originalRevision: number | string;
    revisedRevision: number;
    change: 'Unchanged' | 'Added' | 'Modified' | 'Deleted';
    patch: string;
}

export interface ProductDiffItem {
    productNumber: string;
    change: 'Unchanged' | 'Added' | 'Modified' | 'Deleted';
    patch: string;
    validationResult: any;
}

export interface ProductDiffRequest {
    diffs: ProductDiffItem[];
    validationResult: any;
}

export interface PresentationType {
    nationalMarket: string;
    nationalMarketName: string;
    withDrawalDate: Date;
}

export interface SheetDescriptor {
    index: number;
    name: string;
    required: boolean;
}

export interface BaseFsElement {
    type: 'directory' | 'file';
    name: string;
    parentPath: string;
    directory: boolean;
    metadata?: unknown;
    heathly$?: Observable<boolean>;
}

export interface FsDirectory extends BaseFsElement {
    type: 'directory';
    path: string;
    empty: boolean;
}

export interface FsFile extends BaseFsElement {
    type: 'file';
    uuid: string;
    mimeType: string;
    size: number;
    metadata?: AttachmentRequest;
}

export type FsElement = FsFile | FsDirectory;

export interface FileStorageRequest {
    baseDir: string;
    directories: FsDirectory[];
}

export interface WalkTreeDirectory {
    name: string;
    path: string;
    children: WalkTreeDirectory[];
}
